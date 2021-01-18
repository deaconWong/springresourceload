
local keys,values = KEYS,ARGV

local returnList = {}  
local key 
local index = 1
for i, v in  ipairs(keys) do 
	key = "stationOnline:" .. keys[i] .. "_CLOUDJC"
 	if (redis.call('EXISTS', key) == 1 )
 		then
			 returnList[index] = "{\""..v.."\""..":".. redis.call('get', key) .. "}"
			 index = index + 1
 		end
	end
 
return  returnList 