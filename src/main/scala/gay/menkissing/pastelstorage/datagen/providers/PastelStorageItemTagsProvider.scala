package gay.menkissing.pastelstorage.datagen.providers

import earth.terrarium.pastel.registries.PastelItems
import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.registries.PastelStorageTags
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.PackOutput
import net.minecraft.data.tags.{ItemTagsProvider, TagsProvider}
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.{ItemTags, TagKey}
import net.minecraft.world.item.{Item, Items}
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.common.Tags
import net.neoforged.neoforge.common.data.{BlockTagsProvider, ExistingFileHelper}

import java.util.concurrent.CompletableFuture

class PastelStorageItemTagsProvider
  (output: PackOutput,
   lookupProvider: CompletableFuture[HolderLookup.Provider],
   tagLookup: CompletableFuture[TagsProvider.TagLookup[Block]],
   existingFileHelper: ExistingFileHelper)
  extends ItemTagsProvider(output, lookupProvider, tagLookup, PastelStorage.ModId, existingFileHelper):

  def commonTag(path: String): TagKey[Item] =
    TagKey.create(Registries.ITEM, PastelStorageTags.commonTag(path))
  
  override protected def addTags(provider: HolderLookup.Provider): Unit =
    
    tag(PastelStorageTags.item.validToolTag)
      .addTag(Tags.Items.TOOLS)
      .add(Items.SPYGLASS)
      .add(Items.CLOCK)
      .add(Items.FLINT_AND_STEEL)
      .add(PastelItems.TUNING_STAMP.get())
      .add(PastelItems.OMNI_ACCELERATOR.get())
      .add(PastelItems.CRESCENT_CLOCK.get())
      .add(PastelItems.RADIANCE_STAFF.get())
      .add(PastelItems.NATURES_STAFF.get())
      .add(PastelItems.STAFF_OF_REMEMBRANCE.get())
      .add(PastelItems.CONSTRUCTORS_STAFF.get())
      .add(PastelItems.EXCHANGING_STAFF.get())
      .add(PastelItems.BLOCK_FLOODER.get())
      .add(PastelItems.CELESTIAL_POCKETWATCH.get())
      .add(PastelItems.PAINTBRUSH.get())
      .addOptional(ResourceLocation.fromNamespaceAndPath("botania", "twig_wand"))
      .addOptional(ResourceLocation.fromNamespaceAndPath("botania", "dreamwood_wand"))
      .addTag(ItemTags.COMPASSES)
      .addTag(Tags.Items.TOOLS_WRENCH)
      .addTag(Tags.Items.TOOLS_SHEAR)
      .addTag(Tags.Items.TOOLS_BOW)
      .addTag(Tags.Items.TOOLS_FISHING_ROD)
      .addOptionalTag(ResourceLocation.fromNamespaceAndPath("botania", "rods"))
      .addOptionalTag(ResourceLocation.fromNamespaceAndPath("hexcasting", "staves"))
    
    
