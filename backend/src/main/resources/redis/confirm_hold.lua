-- confirm_hold.lua
-- KEYS: mapKey; ARGV: holdToken
-- Verify that all keys referenced by mapKey have values starting with holdToken
-- If verification passes, delete keys and the mapKey and return 'OK'

local mapKey = KEYS[1]
local holdToken = ARGV[1]

local seats = redis.call('LRANGE', mapKey, 0, -1)
if not seats or #seats == 0 then
  return {err = 'NOT_FOUND'}
end

for i, k in ipairs(seats) do
  local v = redis.call('GET', k)
  if not v or string.sub(v, 1, string.len(holdToken)) ~= holdToken then
    return {err = 'MISMATCH:' .. k}
  end
end

for i, k in ipairs(seats) do
  redis.call('DEL', k)
end
redis.call('DEL', mapKey)
return {'OK'}
