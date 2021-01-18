
local keys,args = KEYS,ARGV

local returnList = {}  
local key
local index = 1
for i, v in  ipairs(keys) do 
	key = "deviceOnline:" .. args[1] .. "_" .. keys[i]
 	if (redis.call('EXISTS', key) == 1 )
 		then
			 returnList[index] = "{\""..v.."\""..":".. redis.call('get', key) .. "}"
			 index = index + 1
 		end
	end
 
return  returnList 