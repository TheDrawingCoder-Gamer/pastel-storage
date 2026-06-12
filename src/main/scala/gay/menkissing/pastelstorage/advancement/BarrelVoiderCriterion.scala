package gay.menkissing.pastelstorage.advancement

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.registries.PastelStorageCriteria
import net.minecraft.advancements.Criterion
import net.minecraft.advancements.critereon.{ContextAwarePredicate, EntityPredicate, ItemPredicate, SimpleCriterionTrigger}
import net.minecraft.core.BlockPos
import net.minecraft.server.level.{ServerLevel, ServerPlayer}
import net.minecraft.world.item.ItemStack

import java.util.Optional


// "Why are your classes final"
// Why AREN'T your classes final if you don't expect them to be extended?
final class BarrelVoiderCriterion extends SimpleCriterionTrigger[BarrelVoiderCriterion.Conditions]:
  def trigger(player: ServerPlayer, input: ItemStack): Unit =
    this.trigger(player, _.matches(input))

  override def codec(): Codec[BarrelVoiderCriterion.Conditions] =
    BarrelVoiderCriterion.Conditions.CODEC
object BarrelVoiderCriterion:
  val Id = PastelStorage.locate("barrel_voider")

  def instance(player: ContextAwarePredicate | Null, voidType: ItemPredicate): Criterion[Conditions] =
    PastelStorageCriteria.BARREL_VOIDING.createCriterion(Conditions(Optional.ofNullable(player), voidType))
  
  final case class Conditions(player: Optional[ContextAwarePredicate], voidType: ItemPredicate) extends SimpleCriterionTrigger.SimpleInstance:
    def matches(voider: ItemStack): Boolean =
      voidType.test(voider)

  object Conditions:
    val CODEC: Codec[Conditions] =
      RecordCodecBuilder.create: builder =>
        builder.group(
          EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter((it: Conditions) => it.player),
          ItemPredicate.CODEC.fieldOf("voider").forGetter((it: Conditions) => it.voidType)
        ).apply(builder, Conditions.apply)