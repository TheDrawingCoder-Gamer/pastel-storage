package gay.menkissing.pastelstorage.advancement

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import gay.menkissing.pastelstorage.PastelStorage
import net.minecraft.advancements.critereon.{ContextAwarePredicate, EntityPredicate, ItemPredicate, SimpleCriterionTrigger}
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

import java.util.Optional

final class InsertIntoVoidBarrelCriterion extends SimpleCriterionTrigger[InsertIntoVoidBarrelCriterion.Conditions]:
  def trigger(player: ServerPlayer, stack: ItemStack): Unit =
    this.trigger(player, _.matches(stack))
  
  override def codec(): Codec[InsertIntoVoidBarrelCriterion.Conditions] =
    InsertIntoVoidBarrelCriterion.Conditions.CODEC

object InsertIntoVoidBarrelCriterion:
  val ID: ResourceLocation = PastelStorage.locate("insert_into_void_barrel")
  
  final case class Conditions(
                             player: Optional[ContextAwarePredicate],
                             inserted: ItemPredicate
                             ) extends SimpleCriterionTrigger.SimpleInstance:
    def matches(item: ItemStack): Boolean =
      inserted.test(item)

  object Conditions:
    val CODEC: Codec[Conditions] =
      RecordCodecBuilder.create: builder =>
        builder.group(
          EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter((it: Conditions) => it.player),
          ItemPredicate.CODEC.fieldOf("inserted").forGetter((it: Conditions) => it.inserted)
        ).apply(builder, Conditions.apply)


