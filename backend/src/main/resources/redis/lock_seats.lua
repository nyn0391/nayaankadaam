-- lock_seats.lua
-- KEYS: seat keys...; ARGV: holdToken, userId, ttlSeconds, mapKey
-- If any seat key exists, return a table of conflicting keys
-- Otherwise set each seat key to holdToken:userId and push keys to mapKey list and set TTLs

local holdToken = ARGV[1]
local userId = ARGV[2]
local ttl = tonumber(ARGV[3])
local mapKey = ARGV[4]

-- check conflicts
for i, k in ipairs(KEYS) do
  if redis.call('EXISTS', k) == 1 then
    return {err = 'CONFLICT:' .. k}
  end
end

-- set keys
for i, k in ipairs(KEYS) do
  redis.call('SET', k, holdToken .. ':' .. userId)
  redis.call('PEXPIRE', k, ttl * 1000)
end

-- store mapping
for i, k in ipairs(KEYS) do
  redis.call('RPUSH', mapKey, k)
end
redis.call('PEXPIRE', mapKey, ttl * 1000)

return {'OK'}
