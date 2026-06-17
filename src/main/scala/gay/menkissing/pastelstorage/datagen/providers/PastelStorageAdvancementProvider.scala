package gay.menkissing.pastelstorage.datagen.providers

import com.cmdpro.databank.advancement.criteria.HasAdvancementCriteria
import com.cmdpro.databank.registry.CriteriaTriggerRegistry
import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.advancement.{BarrelVoiderCriterion, InsertIntoVoidBarrelCriterion}
import net.minecraft.advancements.critereon.{ImpossibleTrigger, ItemPredicate}
import net.minecraft.advancements.{Advancement, AdvancementHolder, AdvancementType, CriteriaTriggers, Criterion}
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.advancements.AdvancementSubProvider
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.neoforged.neoforge.common.data.{AdvancementProvider, ExistingFileHelper}

import java.util.concurrent.CompletableFuture
import java.util as ju
import java.util.Optional
import java.util.function.Consumer

final class PastelStorageAdvancementProvider
  (output: PackOutput,
   lookupProvider: CompletableFuture[HolderLookup.Provider],
   existingFileHelper: ExistingFileHelper) extends AdvancementProvider(output, lookupProvider, existingFileHelper, ju.List.of(new PastelStorageAdvancementProvider.PastelStorageAdvancementGenerator))


object PastelStorageAdvancementProvider:
  def wrapHasAdvancement(name: ResourceLocation): Criterion[HasAdvancementCriteria.HasAdvancementCriteriaInstance] =
    CriteriaTriggerRegistry.HAS_ADVANCEMENT.get().createCriterion(HasAdvancementCriteria.HasAdvancementCriteriaInstance(Optional.empty(), name))

  private final class PastelStorageAdvancementGenerator extends AdvancementProvider.AdvancementGenerator:
    override def generate(provider: HolderLookup.Provider, saver: Consumer[AdvancementHolder], existingFileHelper: ExistingFileHelper): Unit =
      val collectedReeds = wrapHasAdvancement(ResourceLocation.fromNamespaceAndPath("pastel", "collect_quitoxic_reeds"))
      val collectedStratine = wrapHasAdvancement(ResourceLocation.fromNamespaceAndPath("pastel", "midgame/collect_stratine"))
      val collectedOnyx = wrapHasAdvancement(ResourceLocation.fromNamespaceAndPath("pastel", "create_onyx_shard"))
      val impossible = CriteriaTriggers.IMPOSSIBLE.createCriterion(ImpossibleTrigger.TriggerInstance())

      Advancement.Builder.recipeAdvancement()
                 .addCriterion("impossible", impossible)
                 .save(saver, PastelStorage.locate("unlocks/blocks/bottomless_amphora"), existingFileHelper)

      Advancement.Builder.recipeAdvancement()
                 .addCriterion("impossible", impossible)
                 .save(saver, PastelStorage.locate("unlocks/blocks/bottomless_barrel"), existingFileHelper)

      Advancement.Builder.recipeAdvancement()
                 .addCriterion("collected_quitoxic_reeds", collectedReeds)
                 .save(saver, PastelStorage.locate("unlocks/blocks/bottomless_shelf"), existingFileHelper)

      Advancement.Builder.recipeAdvancement()
                 .addCriterion("collected_onyx", collectedOnyx)
                 .save(saver, PastelStorage.locate("unlocks/blocks/filter_chest"), existingFileHelper)

      Advancement.Builder.recipeAdvancement()
                 .addCriterion("collected_quitoxic_reeds", collectedReeds)
                 .save(saver, PastelStorage.locate("unlocks/items/bottomless_bottle"), existingFileHelper)

      Advancement.Builder.recipeAdvancement()
                 .addCriterion("collected_quitoxic_reeds", collectedReeds)
                 .save(saver, PastelStorage.locate("unlocks/items/bottomless_battery"), existingFileHelper)

      Advancement.Builder.recipeAdvancement()
                 .addCriterion("collected_quitoxic_reeds", collectedReeds)
                 .save(saver, PastelStorage.locate("unlocks/items/tool_container"), existingFileHelper)

      // TODO: get this to work with the parent
      /*
      Advancement.Builder.advancement()
                 .parent(AdvancementSubProvider.createPlaceholder("pastel:midgame/pastel_midgame"))
                 .display(
                   Items.LAVA_BUCKET,
                   Component.translatable("advancements.pastelstorage.lava_in_voider.title"),
                   Component.translatable("advancements.pastelstorage.lava_in_voider.description"),
                   null,
                   AdvancementType.TASK,
                   true, // show in toast
                   true, // show in chat
                   true // hidden true
                 )
                 .addCriterion("gotten_previous", wrapHasAdvancement(ResourceLocation.fromNamespaceAndPath("pastel", "midgame/pastel_midgame")))
                 .addCriterion("inserted_lava",
                   InsertIntoVoidBarrelCriterion.instance(null, ItemPredicate.Builder.item().of(Items.LAVA_BUCKET).build())
                 )
                 .addCriterion("observed_lava_voiding", BarrelVoiderCriterion.instance(null, ItemPredicate.Builder.item().of(Items.LAVA_BUCKET).build()))
                 .save(saver, PastelStorage.locate("lava_in_voider"), existingFileHelper)
      */