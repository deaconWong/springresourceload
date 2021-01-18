package deacon.test.spb.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

/**
 * @author wanglm
 * @date 2020/09/11
 */
public class FileUtils {
    private static Logger log = LoggerFactory.getLogger(FileUtils.class);
    
    private static final String PROTOCOL_FILE_PATH_SEPERATOR = "/";
    
    private FileUtils() {}

    /**
     * 根据文件指针偏移量, 读取文件的下一行内容
     * 
     * 1. 已读取到文件结尾:
     *  EOF:true, RESULT:false
     * 2. 有数据时:
     *  EOF:false或true, 为true时, 表示读到最后一行了
     *  result:true
     * 3. 异常出现时:
     *  EOF:false, RESULT:false
     * 
     * @param sourceFile
     * @param offset
     * @return
     * @throws IOException
     */
    public static JSONObject readAppointedLineNumber(File sourceFile, long offset) {
        JSONObject contentJson = new JSONObject();
        contentJson.put("fileName", sourceFile.getParentFile().getName() +  PROTOCOL_FILE_PATH_SEPERATOR
            + sourceFile.getName());
        String content = null;
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(sourceFile, "r")) {
            randomAccessFile.seek(offset); // 移动文件记录指针的位置,
            // 处理文件结尾
            if (randomAccessFile.getFilePointer() >= randomAccessFile.length()) {
                log.debug("文件指针偏移量已超过文件:{}最大长度:{}, 默认为读取到文件结尾", contentJson, randomAccessFile.length());
                contentJson.put("eof", true); //达到文件结尾
                contentJson.put("resutl", false); //没有实际数据
                return contentJson;
            }
            // ~

            // 自动跳过空行或空白行
            while ((content = randomAccessFile.readLine()).trim().isEmpty()) {
                // 到达文件结尾
                if (randomAccessFile.getFilePointer() >= randomAccessFile.length()) {
                    content = "";
                    break;
                }
                randomAccessFile.skipBytes(content.length());
            }
            // ~
            contentJson.put("resutl", !content.isEmpty()); //是否有数据
            contentJson.put("content", new String(content.getBytes(StandardCharsets.ISO_8859_1),
                StandardCharsets.UTF_8));
            contentJson.put("offset", randomAccessFile.getFilePointer());
            // 是否将文件读完了
            contentJson.put("eof", sourceFile.length() == randomAccessFile.getFilePointer());

        } catch (Exception e) {
            log.error("根据偏移量:{}读取文件:{}时出错:{}", offset, sourceFile, e.getMessage(), e);
            contentJson.put("offset", offset); // 出错时保留原有文件读取偏移量
            contentJson.put("content", ""); // 出错时内容为空
            contentJson.put("resutl", false);
        }
        return contentJson;
    }

    // 文件内容的总行数。
    @SuppressWarnings("unused")
    private static long getTotalLines(File file) throws IOException {
        long lines = 0;
        try (LineNumberReader reader = new LineNumberReader(new FileReader(file))) {
            lines = reader.lines().count();
        } catch (Exception e) {
            log.error("获取文件总行数时失败:{}", e.getMessage(), e);
            lines = 0;
        }
        return lines;
    }

    /**
     * 读取当天日志内容中, 最近一次的错误日志所在行的所有内容 like: 2020-07-29 11:25:59.627
     * [http-nio-8080-exec-8] ERROR xxx
     * 
     * @param sourceFile
     * @param offset
     * @return content: 错误信息内容 lineNumber: 读取文件的最大行号
     */
    public static Map<String, Object> readAppointedExceptionLine(String filePath, int readedLineNumber) {
        Optional<String> lastErrorInfo;
        Map<String, Object> resultMap = new HashMap<>(2);
        try (InputStream is = Files.newInputStream(Paths.get(filePath), StandardOpenOption.READ);
            LineNumberReader reader = new LineNumberReader(new BufferedReader(new InputStreamReader(is,
                StandardCharsets.UTF_8)))) {

            lastErrorInfo = reader.lines().filter(line -> line.indexOf("ERROR") != -1).sorted(Comparator.reverseOrder())
                .findFirst();
            if (reader.getLineNumber() <= readedLineNumber) {// 避免重复读取一行错误内容
                lastErrorInfo = Optional.empty();
            } else {
                resultMap.put("lineNumber", reader.getLineNumber());
            }
        } catch (Exception e) {
            log.error("读取检测平台日志错误内容时失败:{}", e.getMessage(), e);
            lastErrorInfo = Optional.empty();
        }
        resultMap.put("content", lastErrorInfo.orElse(""));
        return resultMap;
    }

}
