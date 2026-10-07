; Horned Blade - comandos (estilo JUS: direccion + boton, con alternativas
; de movimiento clasico para los especiales y supers).
;
; a = corte debil   b = Great Cleave   c = golpe fuerte
; x = Storm Hammer  y = Gran Hendidura z = Warcry
; s (mantener) = cargar poder

[Remap]
x = x
y = y
z = z
a = a
b = b
c = c
s = s

[Defaults]
command.time = 15
command.buffer.time = 1

;=============================================================== SUPERS
[Command]
name = "ultimate"
command = x+y+z
time = 3

[Command]
name = "ultimate"
command = ~D, DB, B, D, DB, B, z
time = 30

[Command]
name = "gods"
command = x+y
time = 2

[Command]
name = "gods"
command = ~D, DF, F, D, DF, F, x
time = 30

[Command]
name = "stormcleave"
command = y+z
time = 2

[Command]
name = "stormcleave"
command = ~D, DF, F, D, DF, F, y
time = 30

;=============================================================== ESPECIALES
[Command]
name = "hammerrush"
command = ~F, D, DF, x
time = 18

[Command]
name = "hammerrush"
command = /F, x
time = 1

[Command]
name = "risingcleave"
command = ~F, D, DF, y
time = 18

[Command]
name = "risingcleave"
command = /F, y
time = 1

[Command]
name = "stormhammer"
command = ~D, DF, F, x
time = 15

[Command]
name = "stormhammer"
command = x
time = 1

[Command]
name = "greatcleave"
command = ~D, DB, B, y
time = 15

[Command]
name = "greatcleave"
command = y
time = 1

[Command]
name = "warcry"
command = ~D, D, z
time = 15

[Command]
name = "warcry"
command = z
time = 1

;=============================================================== MOVIMIENTO
[Command]
name = "FF"     ;Requerido
command = F, F
time = 10

[Command]
name = "BB"     ;Requerido
command = B, B
time = 10

[Command]
name = "recovery" ;Requerido
command = x+y
time = 1

[Command]
name = "recovery"
command = a+b
time = 1

;=============================================================== BOTONES
[Command]
name = "a"
command = a
time = 1

[Command]
name = "b"
command = b
time = 1

[Command]
name = "c"
command = c
time = 1

[Command]
name = "x"
command = x
time = 1

[Command]
name = "y"
command = y
time = 1

[Command]
name = "z"
command = z
time = 1

[Command]
name = "s"
command = s
time = 1

[Command]
name = "fwd_c"
command = /F, c
time = 1

[Command]
name = "back_c"
command = /B, c
time = 1

[Command]
name = "taunt"
command = /D, s
time = 1

;=============================================================== MANTENER
[Command]
name = "holds"
command = /s
time = 1

[Command]
name = "holdfwd"  ;Requerido
command = /$F
time = 1

[Command]
name = "holdback" ;Requerido
command = /$B
time = 1

[Command]
name = "holdup"   ;Requerido
command = /$U
time = 1

[Command]
name = "holddown" ;Requerido
command = /$D
time = 1

;===============================================================
; Estado -1: entrada a los movimientos (jugador humano y CPU)
; var(59) = 1 si el personaje lo controla la CPU (ver estado -2)
;===============================================================
[Statedef -1]

;------------------------------------------------ SUPERS (humano)
[State -1, Ultimate]
type = ChangeState
value = 3200
triggerall = !var(59)
triggerall = command = "ultimate"
triggerall = power >= 3000
triggerall = statetype != A
trigger1 = ctrl
trigger2 = (stateno = [200, 330]) && movecontact
trigger3 = (stateno = [1000, 1299]) && movecontact

[State -1, God's Strength]
type = ChangeState
value = 3000
triggerall = !var(59)
triggerall = command = "gods"
triggerall = power >= 1000
triggerall = var(11) = 0
triggerall = statetype != A
trigger1 = ctrl
trigger2 = (stateno = [200, 330]) && movecontact

[State -1, Storm Cleave]
type = ChangeState
value = 3100
triggerall = !var(59)
triggerall = command = "stormcleave"
triggerall = power >= 1000
triggerall = statetype != A
trigger1 = ctrl
trigger2 = (stateno = [200, 330]) && movecontact
trigger3 = (stateno = [1000, 1299]) && movecontact

;------------------------------------------------ ESPECIALES (humano)
[State -1, Hammer Rush]
type = ChangeState
value = 1100
triggerall = !var(59)
triggerall = command = "hammerrush"
triggerall = statetype != A
trigger1 = ctrl
trigger2 = (stateno = [200, 330]) && movecontact

[State -1, Rising Cleave]
type = ChangeState
value = 1250
triggerall = !var(59)
triggerall = command = "risingcleave"
triggerall = statetype != A
trigger1 = ctrl
trigger2 = (stateno = [200, 330]) && movecontact

[State -1, Storm Hammer aereo]
type = ChangeState
value = 1050
triggerall = !var(59)
triggerall = command = "stormhammer"
triggerall = statetype = A
triggerall = numprojid(1000) = 0
trigger1 = ctrl
trigger2 = (stateno = [600, 630]) && movecontact

[State -1, Storm Hammer]
type = ChangeState
value = 1000
triggerall = !var(59)
triggerall = command = "stormhammer"
triggerall = statetype != A
triggerall = numprojid(1000) = 0
trigger1 = ctrl
trigger2 = (stateno = [200, 330]) && movecontact

[State -1, Great Cleave]
type = ChangeState
value = 1200
triggerall = !var(59)
triggerall = command = "greatcleave"
triggerall = statetype != A
trigger1 = ctrl
trigger2 = (stateno = [200, 330]) && movecontact

[State -1, Warcry]
type = ChangeState
value = 1300
triggerall = !var(59)
triggerall = command = "warcry"
triggerall = var(12) = 0
triggerall = statetype != A
trigger1 = ctrl
trigger2 = (stateno = [200, 330]) && movecontact

;------------------------------------------------ SISTEMA (humano)
[State -1, Correr]
type = ChangeState
value = 100
triggerall = !var(59)
trigger1 = command = "FF"
trigger1 = statetype = S
trigger1 = ctrl

[State -1, Esquiva atras]
type = ChangeState
value = 105
triggerall = !var(59)
trigger1 = command = "BB"
trigger1 = statetype = S
trigger1 = ctrl

[State -1, Dash aereo]
type = ChangeState
value = 110
triggerall = !var(59)
triggerall = command = "FF"
triggerall = statetype = A
triggerall = var(5) = 0
triggerall = pos y < -20
trigger1 = ctrl

[State -1, Burla]
type = ChangeState
value = 195
triggerall = !var(59)
triggerall = command = "taunt"
trigger1 = statetype = S
trigger1 = ctrl

[State -1, Cargar poder]
type = ChangeState
value = 700
triggerall = !var(59)
triggerall = command = "holds"
triggerall = command != "holddown"
triggerall = power < powermax
trigger1 = statetype = S
trigger1 = ctrl

;------------------------------------------------ NORMALES (humano)
[State -1, Agarre]
type = ChangeState
value = 800
triggerall = !var(59)
triggerall = command = "fwd_c" || command = "back_c"
triggerall = statetype = S
triggerall = ctrl
trigger1 = p2bodydist x < 12
trigger1 = p2statetype != A
trigger1 = p2movetype != H

[State -1, Embestida]
type = ChangeState
value = 250
triggerall = !var(59)
triggerall = command = "fwd_c"
triggerall = statetype = S
trigger1 = ctrl
trigger2 = (stateno = 200 || stateno = 210 || stateno = 230) && movecontact

[State -1, Combo a 2]
type = ChangeState
value = 210
triggerall = !var(59)
triggerall = command = "a"
triggerall = command != "holddown"
trigger1 = stateno = 200 && movecontact

[State -1, Combo a 3]
type = ChangeState
value = 220
triggerall = !var(59)
triggerall = command = "a"
triggerall = command != "holddown"
trigger1 = stateno = 210 && movecontact

[State -1, Corte rapido]
type = ChangeState
value = 200
triggerall = !var(59)
triggerall = command = "a"
triggerall = command != "holddown"
trigger1 = statetype = S
trigger1 = ctrl

[State -1, Great Cleave (normal)]
type = ChangeState
value = 230
triggerall = !var(59)
triggerall = command = "b"
triggerall = command != "holddown"
trigger1 = statetype = S
trigger1 = ctrl
trigger2 = (stateno = 200 || stateno = 210 || stateno = 300) && movecontact

[State -1, Golpe demoledor]
type = ChangeState
value = 240
triggerall = !var(59)
triggerall = command = "c"
triggerall = command != "holddown"
trigger1 = statetype = S
trigger1 = ctrl
trigger2 = (stateno = 200 || stateno = 210 || stateno = 230 || stateno = 300 || stateno = 310) && movecontact

[State -1, Corte bajo]
type = ChangeState
value = 300
triggerall = !var(59)
triggerall = command = "a"
triggerall = command = "holddown"
trigger1 = statetype = C
trigger1 = ctrl

[State -1, Tajo lunar]
type = ChangeState
value = 310
triggerall = !var(59)
triggerall = command = "b"
triggerall = command = "holddown"
trigger1 = statetype = C
trigger1 = ctrl
trigger2 = (stateno = 300 || stateno = 200 || stateno = 210) && movecontact

[State -1, Barrida]
type = ChangeState
value = 320
triggerall = !var(59)
triggerall = command = "c"
triggerall = command = "holddown"
trigger1 = statetype = C
trigger1 = ctrl
trigger2 = (stateno = 300 || stateno = 310) && movecontact

[State -1, Aereo a]
type = ChangeState
value = 600
triggerall = !var(59)
triggerall = command = "a"
triggerall = statetype = A
trigger1 = ctrl
trigger2 = stateno = 610 && movecontact

[State -1, Aereo b]
type = ChangeState
value = 610
triggerall = !var(59)
triggerall = command = "b"
triggerall = statetype = A
trigger1 = ctrl
trigger2 = stateno = 600 && movecontact

[State -1, Aereo c]
type = ChangeState
value = 620
triggerall = !var(59)
triggerall = command = "c"
triggerall = statetype = A
trigger1 = ctrl
trigger2 = (stateno = 600 || stateno = 610) && movecontact

;===============================================================
; IA (solo cuando la CPU controla al personaje)
;===============================================================
[State -1, IA Ultimate]
type = ChangeState
value = 3200
triggerall = var(59)
triggerall = power >= 3000 && statetype != A
triggerall = p2bodydist x < 150 && p2statetype != L && p2movetype != H
trigger1 = ctrl && random < 60
trigger2 = (stateno = [200, 330]) && movehit && random < 500

[State -1, IA God's Strength]
type = ChangeState
value = 3000
triggerall = var(59)
triggerall = power >= 1000 && var(11) = 0 && statetype != A
trigger1 = ctrl && p2bodydist x > 90 && random < 40
trigger2 = ctrl && life < 500 && random < 80

[State -1, IA Storm Cleave]
type = ChangeState
value = 3100
triggerall = var(59)
triggerall = power >= 1000 && statetype != A && var(11) > 0
triggerall = p2bodydist x < 140 && p2statetype != L
trigger1 = ctrl && random < 40
trigger2 = (stateno = [200, 330]) && movehit && random < 300

[State -1, IA anti-aereo]
type = ChangeState
value = 1250
triggerall = var(59)
triggerall = statetype != A
triggerall = p2statetype = A && p2bodydist x < 55 && p2bodydist y < -20 && p2movetype != H
trigger1 = ctrl && random < 350

[State -1, IA Warcry]
type = ChangeState
value = 1300
triggerall = var(59)
triggerall = var(12) = 0 && statetype = S
trigger1 = ctrl && p2bodydist x > 110 && random < 60
trigger2 = ctrl && life < 400 && p2bodydist x > 70 && random < 120

[State -1, IA Storm Hammer]
type = ChangeState
value = 1000
triggerall = var(59)
triggerall = numprojid(1000) = 0 && statetype != A
trigger1 = ctrl && p2bodydist x > 100 && random < 45
trigger2 = (stateno = 220 || stateno = 230) && movehit && random < 250

[State -1, IA Storm Hammer aereo]
type = ChangeState
value = 1050
triggerall = var(59)
triggerall = numprojid(1000) = 0 && statetype = A && ctrl
trigger1 = p2bodydist x > 60 && p2bodydist x < 160 && random < 50

[State -1, IA Hammer Rush]
type = ChangeState
value = 1100
triggerall = var(59)
triggerall = statetype != A
trigger1 = ctrl && p2bodydist x > 70 && p2bodydist x < 200 && p2statetype != A && random < 25
trigger2 = (stateno = 220) && movehit && random < 300

[State -1, IA Gran Hendidura]
type = ChangeState
value = 1200
triggerall = var(59)
triggerall = statetype != A
trigger1 = ctrl && p2bodydist x > 40 && p2bodydist x < 170 && random < 35
trigger2 = (stateno = 210 || stateno = 310) && movehit && random < 300

[State -1, IA cargar]
type = ChangeState
value = 700
triggerall = var(59)
triggerall = statetype = S && ctrl
trigger1 = power < 3000 && p2bodydist x > 170 && random < 60
trigger2 = power < 1000 && p2bodydist x > 130 && random < 40

[State -1, IA agarre]
type = ChangeState
value = 800
triggerall = var(59)
triggerall = statetype = S && ctrl
trigger1 = p2bodydist x < 10 && p2statetype = S && p2movetype != H && random < 120

[State -1, IA combo 2]
type = ChangeState
value = 210
triggerall = var(59)
trigger1 = stateno = 200 && movecontact && random < 850

[State -1, IA combo 3]
type = ChangeState
value = 220
triggerall = var(59)
trigger1 = stateno = 210 && movecontact && random < 750

[State -1, IA lanzador a aire]
type = ChangeState
value = 310
triggerall = var(59)
trigger1 = stateno = 300 && movehit && random < 600

[State -1, IA corte rapido]
type = ChangeState
value = 200
triggerall = var(59)
triggerall = statetype = S && ctrl
trigger1 = p2bodydist x < 35 && p2statetype != L && random < 160

[State -1, IA golpe demoledor]
type = ChangeState
value = 240
triggerall = var(59)
triggerall = statetype = S && ctrl
trigger1 = p2bodydist x < 55 && p2statetype != L && random < 50
trigger2 = p2statetype = L && p2bodydist x < 50 && random < 25

[State -1, IA Great Cleave]
type = ChangeState
value = 230
triggerall = var(59)
triggerall = statetype = S && ctrl
trigger1 = p2bodydist x < 60 && p2statetype != L && random < 70

[State -1, IA bajo]
type = ChangeState
value = 300
triggerall = var(59)
triggerall = statetype != A && ctrl
trigger1 = p2bodydist x < 40 && p2statetype = C && random < 120

[State -1, IA barrida]
type = ChangeState
value = 320
triggerall = var(59)
triggerall = statetype != A && ctrl
trigger1 = p2bodydist x < 50 && p2statetype != A && random < 35

[State -1, IA aereos]
type = ChangeState
value = 600 + 10 * (random % 3)
triggerall = var(59)
triggerall = statetype = A && ctrl
trigger1 = p2bodydist x < 45 && abs(p2bodydist y) < 60 && random < 220

[State -1, IA correr]
type = ChangeState
value = 100
triggerall = var(59)
triggerall = statetype = S && ctrl
trigger1 = p2bodydist x > 140 && random < 30

[State -1, IA saltar]
type = ChangeState
value = 40
triggerall = var(59)
triggerall = statetype = S && ctrl
trigger1 = p2bodydist x < 120 && p2bodydist x > 50 && random < 12
