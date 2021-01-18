
local keys,values = KEYS,ARGV

local returnList = {}  
 
local itemKey = values[1]
 local index = 1
for i,v in ipairs(keys) do  
 	if (redis.call('HEXISTS', itemKey, v) == 1 ) then
			 returnList[index] = "{\""..v.."\""..":".. redis.call('HGET', itemKey , v) .."}"
			 index = index + 1
 		end
	end
 
return  returnList 