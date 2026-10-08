import json, sys
ssj4_code, ssj5_code, out = open(sys.argv[1]).read().strip(), open(sys.argv[2]).read().strip(), sys.argv[3]
G = "afsaiyan"

def form(name, lvl, **kw):
    # Mirrors DefaultFormsFactory.setDefaultMasteryValues + the per-form fields DMZ 2.1.3 sets on its Saiyan forms
    d = {
        "name": name, "unlockOnSkillLevel": lvl, "formCombo": "", "customModel": "",
        "keepBaseFormHeadBones": False, "transformationAnimation": "transf.generic",
        "bodyColor1": "", "bodyColor2": "", "bodyColor3": "", "extraFormLayer": "", "extraFormColor": "",
        "hairType": "", "forcedHairCode": "", "hairColor": "", "eye1Color": "", "eye2Color": "",
        "auraType": "kakarot", "auraLayer": 0, "auraColor": "", "extraAuraLayer": -1,
        "extraAuraColor": "#FFFFFF", "extraAuraType": "kakarot",
        "hasLightnings": False, "lightningColor": "", "tintColor": "#FF0000", "tintIntensity": 0.0,
        "modelScaling": [0.9375, 0.9375, 0.9375],
        "strMultiplier": 1.0, "skpMultiplier": 1.0, "stmMultiplier": 1.0, "defMultiplier": 1.0,
        "vitMultiplier": 1.0, "pwrMultiplier": 1.0, "eneMultiplier": 1.0, "speedMultiplier": 1.0,
        "staminaDrainMultiplier": 1.0, "energyDrain": 0.0, "staminaDrain": 0.0, "healthDrain": 0.0,
        "attackSpeed": 1.0, "maxMastery": 100.0, "masteryPerHitDealt": 0.04, "masteryPerHitReceived": 0.04,
        "passiveMasteryEveryFiveSeconds": 0.006, "maxCostMultiplier": 0.75, "maxStatsMultiplier": 1.5,
        "formRequisite": "", "formRequisiteType": "all", "unlockOnMastery": 0.0, "stackOnMastery": 25.0,
        "instantTransformOnMastery": 40.0, "allowFreeTransformOnMastery": 50.0, "formStackable": True,
        "stackDrainMultiplier": 2.0, "incompatibleWith": [""], "shareMasteryWith": [], "shareMasteryMultiplier": 1.0,
        "triggerItemCosts": [], "durationItemCosts": [], "mobEffects": [],
    }
    d.update(kw)
    return d

# DMZ keys some effects on the form *name*: names containing "supersaiyan2"/"supersaiyan3" get the bigger SSJ2/SSJ3
# aura, and names containing "supersaiyan4"/"ssj4" always show the Saiyan tail (SSJ5 keeps it too, hence "_ssj4").
ssj_eyes = dict(eye1Color="#00FFFF", eye2Color="#00FFFF", auraColor="#FFD700")
forms = {
    # SSJ1-SSJ3: in AF they are the same as in Z, so the look and power copy DMZ's own Super Saiyan values
    "afsupersaiyan": form("afsupersaiyan", 1, hairType="ssj", hairColor="#FFEDB3", bodyColor2="#FFEDB3", **ssj_eyes,
                   strMultiplier=1.5, skpMultiplier=1.5, defMultiplier=1.3125, pwrMultiplier=1.5,
                   energyDrain=0.08, allowFreeTransformOnMastery=0.0),
    "afsupersaiyan2": form("afsupersaiyan2", 5, hairType="ssj2", hairColor="#FFE89E", bodyColor2="#FFE89E", **ssj_eyes,
                   hasLightnings=True, lightningColor="#A1FFF9",
                   strMultiplier=2.25, skpMultiplier=2.25, defMultiplier=1.8125, pwrMultiplier=2.25, energyDrain=0.16),
    "afsupersaiyan3": form("afsupersaiyan3", 6, hairType="ssj3", transformationAnimation="transf.ssj3", hairColor="#FFE89E",
                   bodyColor2="#FFE89E", **ssj_eyes, hasLightnings=True, lightningColor="#A1FFF9",
                   strMultiplier=3.0, skpMultiplier=3.0, defMultiplier=2.4375, pwrMultiplier=3.0, energyDrain=0.34),
    # SSJ4: GT design (AF continues GT), identical to DMZ's ssj4gt: red fur, golden eyes, black SSJ4 hair
    "afsupersaiyan4": form("afsupersaiyan4", 8, customModel="ssj4gt", transformationAnimation="transf.ozaru", hairType="base",
                   forcedHairCode=ssj4_code, bodyColor2="#9d1e31", eye1Color="#FFD700", eye2Color="#FFD700",
                   auraColor="#FFD700", modelScaling=[0.96, 0.96, 0.96],
                   strMultiplier=3.75, skpMultiplier=3.75, defMultiplier=2.875, pwrMultiplier=3.75, energyDrain=0.24),
    # SSJ5: SSJ4 body with silver-white fur, long silver mane (DMZ SSJ4 hair lengthened), silver eyes, no sparks
    "afsupersaiyan5_ssj4": form("afsupersaiyan5_ssj4", 8, customModel="ssj4gt", transformationAnimation="transf.ozaru", hairType="base",
                   forcedHairCode=ssj5_code, hairColor="#E9EDF2", bodyColor2="#DCE0E6", eye1Color="#9FAAB8",
                   eye2Color="#9FAAB8", auraColor="#EEF3FF", modelScaling=[1.0, 1.0, 1.0],
                   strMultiplier=4.5, skpMultiplier=4.5, defMultiplier=3.375, pwrMultiplier=4.5,
                   energyDrain=0.3, staminaDrainMultiplier=1.1),
}
order = list(forms)
for prev, cur in zip(order, order[1:]):
    forms[cur]["formRequisite"] = f"{G}.{prev}"
    forms[cur]["unlockOnMastery"] = 25.0
forms["afsupersaiyan5_ssj4"]["unlockOnMastery"] = 75.0  # SSJ5 needs SSJ4 (AF) well mastered on top of the last superforms level

cfg = {"configVersion": "2.1.3", "groupName": G, "formType": "superforms", "forms": forms}
with open(out, "w") as f:
    json.dump(cfg, f, indent=2, ensure_ascii=False); f.write("\n")
