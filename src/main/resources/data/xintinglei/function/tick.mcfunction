execute as @a[scores={xtl.en_crash=1}] run effect give @s minecraft:slowness 10 0 true
execute as @a[scores={xtl.en_crash=1}] run effect give @s minecraft:weakness 10 0 true
execute as @a[scores={xtl.mo_crash=1}] run effect give @s minecraft:mining_fatigue 30 0 true
execute as @a[scores={xtl.bo_crash=1}] run effect give @s minecraft:slowness 10 0 true
scoreboard players remove @a[scores={xtl.en_crash=1..}] xtl.en_crash 1
scoreboard players remove @a[scores={xtl.mo_crash=1..}] xtl.mo_crash 1
scoreboard players remove @a[scores={xtl.bo_crash=1..}] xtl.bo_crash 1
