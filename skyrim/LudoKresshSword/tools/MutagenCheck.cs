using Mutagen.Bethesda;
using Mutagen.Bethesda.Plugins;
using Mutagen.Bethesda.Skyrim;

var path = args[0];
var mod = SkyrimMod.CreateFromBinary(new ModPath(path), SkyrimRelease.SkyrimSE);
Console.WriteLine($"ModKey={mod.ModKey} Flags={mod.ModHeader.Flags} Version={mod.ModHeader.Stats.Version} NumRecords={mod.ModHeader.Stats.NumRecords} NextFormID=0x{mod.ModHeader.Stats.NextFormID:X}");
Console.WriteLine($"Author={mod.ModHeader.Author} Desc={mod.ModHeader.Description}");
foreach (var m in mod.ModHeader.MasterReferences) Console.WriteLine($"Master={m.Master}");

foreach (var e in mod.MagicEffects)
{
    Console.WriteLine($"MGEF {e.FormKey} {e.EditorID} '{e.Name}' Flags={e.Flags} BaseCost={e.BaseCost} Skill={e.MagicSkill} Resist={e.ResistValue} Archetype={e.Archetype.GetType().Name} AV={e.Archetype.ActorValue} Cast={e.CastType} Delivery={e.TargetType} HitShader={e.HitShader.FormKey} EnchShader={e.EnchantShader.FormKey} SecondAV={e.SecondActorValue} SoundLvl={e.CastingSoundLevel} DualScale={e.DualCastScale}");
    Console.WriteLine($"     Desc='{e.Description}'");
}
foreach (var en in mod.ObjectEffects)
{
    Console.WriteLine($"ENCH {en.FormKey} {en.EditorID} '{en.Name}' Cost={en.EnchantmentCost} Flags={en.Flags} Cast={en.CastType} Amount={en.EnchantmentAmount} Target={en.TargetType} Type={en.EnchantType} Charge={en.ChargeTime} Base={en.BaseEnchantment.FormKey} Worn={en.WornRestrictions.FormKey}");
    foreach (var ef in en.Effects)
        Console.WriteLine($"     effect {ef.BaseEffect.FormKey} mag={ef.Data?.Magnitude} area={ef.Data?.Area} dur={ef.Data?.Duration}");
}
foreach (var s in mod.Statics)
    Console.WriteLine($"STAT {s.FormKey} {s.EditorID} model={s.Model?.File} bounds={s.ObjectBounds.First}->{s.ObjectBounds.Second} maxAngle={s.MaxAngle} material={s.Material.FormKey}");
foreach (var w in mod.Weapons)
{
    Console.WriteLine($"WEAP {w.FormKey} {w.EditorID} '{w.Name}' model={w.Model?.File} bounds={w.ObjectBounds.First}->{w.ObjectBounds.Second}");
    Console.WriteLine($"     ench={w.ObjectEffect.FormKey} amount={w.EnchantmentAmount} equip={w.EquipmentType.FormKey} bash={w.BlockBashImpact.FormKey} altBlock={w.AlternateBlockMaterial.FormKey} pickup={w.PickUpSound.FormKey} putdown={w.PutDownSound.FormKey}");
    Console.WriteLine($"     keywords={string.Join(",", w.Keywords!.Select(k => k.FormKey.ToString()))}");
    Console.WriteLine($"     impact={w.ImpactDataSet.FormKey} 1st={w.FirstPersonModel.FormKey} attackSnd={w.AttackSound.FormKey} equipSnd={w.EquipSound.FormKey} unequipSnd={w.UnequipSound.FormKey}");
    Console.WriteLine($"     value={w.BasicStats!.Value} weight={w.BasicStats.Weight} damage={w.BasicStats.Damage}");
    var d = w.Data!;
    Console.WriteLine($"     anim={d.AnimationType} speed={d.Speed} reach={d.Reach} flags={d.Flags} fov={d.SightFOV} attackAnim={d.AttackAnimation} proj={d.NumProjectiles} embAV={d.EmbeddedWeaponAV} rmin={d.RangeMin} rmax={d.RangeMax} onHit={d.OnHit} animMult={d.AnimationAttackMult} skill={d.Skill} resist={d.Resist} stagger={d.Stagger}");
    var c = w.Critical!;
    Console.WriteLine($"     crit dmg={c.Damage} mult={c.PercentMult} flags={c.Flags} effect={c.Effect.FormKey}  detection={w.DetectionSoundLevel}");
}
foreach (var r in mod.ConstructibleObjects)
{
    Console.WriteLine($"COBJ {r.FormKey} {r.EditorID} creates={r.CreatedObject.FormKey} x{r.CreatedObjectCount} bench={r.WorkbenchKeyword.FormKey}");
    foreach (var i in r.Items!) Console.WriteLine($"     item {i.Item.Item.FormKey} x{i.Item.Count}");
}
var outPath = args[1];
mod.WriteToBinary(outPath);
Console.WriteLine($"Round-trip written to {outPath}");
