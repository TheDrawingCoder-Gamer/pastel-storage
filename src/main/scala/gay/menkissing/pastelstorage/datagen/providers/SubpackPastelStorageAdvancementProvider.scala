package gay.menkissing.pastelstorage.datagen.providers

import com.cmdpro.databank.advancement.criteria.HasAdvancementCriteria
import com.cmdpro.databank.registry.CriteriaTriggerRegistry
import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.advancement.{BarrelVoiderCriterion, InsertIntoVoidBarrelCriterion}
import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.advancements.{Advancement, AdvancementHolder, AdvancementType, Criterion}
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.advancements.AdvancementSubProvider
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.neoforged.neoforge.common.data.{AdvancementProvider, ExistingFileHelper}

import java.util as ju
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

final class SubpackPastelStorageAdvancementProvider
  (output: PackOutput,
   lookupProvider: CompletableFuture[HolderLookup.Provider],
   existingFileHelper: ExistingFileHelper) extends AdvancementProvider(output, lookupProvider, existingFileHelper, ju.List.of(new SubpackPastelStorageAdvancementProvider.PastelStorageAdvancementGenerator))


object SubpackPastelStorageAdvancementProvider:
  def wrapHasAdvancement(name: ResourceLocation): Criterion[HasAdvancementCriteria.HasAdvancementCriteriaInstance] =
    CriteriaTriggerRegistry.HAS_ADVANCEMENT.get().createCriterion(HasAdvancementCriteria.HasAdvancementCriteriaInstance(Optional.empty(), name))

  private final class PastelStorageAdvancementGenerator extends AdvancementProvider.AdvancementGenerator:
    override def generate(provider: HolderLookup.Provider, saver: Consumer[AdvancementHolder], existingFileHelper: ExistingFileHelper): Unit =
      val collectedReeds = wrapHasAdvancement(ResourceLocation.fromNamespaceAndPath("pastel", "collect_quitoxic_reeds"))
      val collectedStratine = wrapHasAdvancement(ResourceLocation.fromNamespaceAndPath("pastel", "midgame/collect_stratine"))
      val collectedOnyx = wrapHasAdvancement(ResourceLocation.fromNamespaceAndPath("pastel", "create_onyx_shard"))

      Advancement.Builder.recipeAdvancement()
                 .addCriterion("collected_quitoxic_reeds",collectedReeds)
                 .addCriterion("collected_stratine",collectedStratine)
                 .addCriterion("collected_noxwood", wrapHasAdvancement(ResourceLocation.fromNamespaceAndPath("pastel", "lategame/collect_noxwood")))
                 .save(saver, PastelStorage.locate("unlocks/blocks/bottomless_amphora"), existingFileHelper)

      Advancement.Builder.recipeAdvancement()
                 .addCriterion("collected_quitoxic_reeds", collectedReeds)
                 .addCriterion("collected_stratine", collectedStratine)
                 .save(saver, PastelStorage.locate("unlocks/blocks/bottomless_barrel"), existingFileHelper)

