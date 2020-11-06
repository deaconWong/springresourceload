# springresourceload

Using ResourceUtils in springboot project;
like:
 ResourceUtils.getURL("classpath:application-" + activeProfiles[0] + ".yml").toURI().getPath();
 
If you wanna do it, you'd have to add some configuations in pom.xml;
