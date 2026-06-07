package gay.menkissing.spectrumstorage.datagen.providers

import de.dafuqs.spectrum.registries.SpectrumItems
import gay.menkissing.spectrumstorage.SpectrumStorage
import gay.menkissing.spectrumstorage.registries.LumoTags
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

class SpectrumStorageItemTagsProvider
  (output: PackOutput,
   lookupProvider: CompletableFuture[HolderLookup.Provider],
   tagLookup: CompletableFuture[TagsProvider.TagLookup[Block]],
   existingFileHelper: ExistingFileHelper)
  extends ItemTagsProvider(output, lookupProvider, tagLookup, SpectrumStorage.ModId, existingFileHelper):

  def commonTag(path: String): TagKey[Item] =
    TagKey.create(Registries.ITEM, LumoTags.commonTag(path))
  
  override protected def addTags(provider: HolderLookup.Provider): Unit =
    
    tag(LumoTags.item.validToolTag)
      .addTag(Tags.Items.TOOLS)
      .add(Items.SPYGLASS)
      .add(Items.CLOCK)
      .add(Items.FLINT_AND_STEEL)
      .add(SpectrumItems.OMNI_ACCELERATOR.get())
      .add(SpectrumItems.CRESCENT_CLOCK.get())
      .add(SpectrumItems.RADIANCE_STAFF.get())
      .add(SpectrumItems.NATURES_STAFF.get())
      .add(SpectrumItems.STAFF_OF_REMEMBRANCE.get())
      .add(SpectrumItems.CONSTRUCTORS_STAFF.get())
      .add(SpectrumItems.EXCHANGING_STAFF.get())
      .add(SpectrumItems.BLOCK_FLOODER.get())
      .add(SpectrumItems.CELESTIAL_POCKETWATCH.get())
      .add(SpectrumItems.PAINTBRUSH.get())
      .addOptional(ResourceLocation.fromNamespaceAndPath("botania", "twig_wand"))
      .addOptional(ResourceLocation.fromNamespaceAndPath("botania", "dreamwood_wand"))
      .addTag(ItemTags.COMPASSES)
      .addTag(Tags.Items.TOOLS_WRENCH)
      .addTag(Tags.Items.TOOLS_SHEAR)
      .addTag(Tags.Items.TOOLS_BOW)
      .addTag(Tags.Items.TOOLS_FISHING_ROD)
      .addOptionalTag(ResourceLocation.fromNamespaceAndPath("botania", "rods"))
      .addOptionalTag(ResourceLocation.fromNamespaceAndPath("hexcasting", "staves"))
    
    
