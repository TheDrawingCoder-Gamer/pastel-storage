package gay.menkissing.spectrumstorage.registries

import de.dafuqs.spectrum.registries.SpectrumItems
import gay.menkissing.spectrumstorage.SpectrumStorage
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.{ItemTags, TagKey}
import net.minecraft.world.item.{Item, Items}

object LumoTags:
  def commonTag(path: String): ResourceLocation =
    ResourceLocation.fromNamespaceAndPath("c", path)

  object item:
    private def tag(namespace: String, path: String): TagKey[Item] =
      TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path))
    private def commonTag(path: String): TagKey[Item] =
      TagKey.create(Registries.ITEM, LumoTags.commonTag(path))

    val validToolTag: TagKey[Item] =
      TagKey.create(Registries.ITEM, SpectrumStorage.locate("valid_tools"))
      /*
      InfoCollector.instance.tag(Registries.ITEM, "valid_tools")
                   .addTag(ItemTags.SWORDS)
                   .addTag(ItemTags.PICKAXES)
                   .addTag(ItemTags.SHOVELS)
                   .addTag(ItemTags.AXES)
                   .addTag(ItemTags.HOES)
                   .add(ResourceLocation.withDefaultNamespace("spyglass"))
                   .add(ResourceLocation.withDefaultNamespace("clock"))
                   .add(ResourceLocation.withDefaultNamespace("flint_and_steel"))
                   // .add(SpectrumItems.TUNING_STAMP) // R.I.P. : (
                   .add(SpectrumItems.OMNI_ACCELERATOR)
                   .add(SpectrumItems.CRESCENT_CLOCK)
                   .add(SpectrumItems.RADIANCE_STAFF)
                   .add(SpectrumItems.NATURES_STAFF)
                   .add(SpectrumItems.STAFF_OF_REMEMBRANCE)
                   .add(SpectrumItems.CONSTRUCTORS_STAFF)
                   .add(SpectrumItems.EXCHANGING_STAFF)
                   .add(SpectrumItems.BLOCK_FLOODER)
                   .add(SpectrumItems.CELESTIAL_POCKETWATCH)
                   .add(SpectrumItems.PAINTBRUSH)
                   .addOptional(ResourceLocation.fromNamespaceAndPath("botania", "twig_wand"))
                   .addOptional(ResourceLocation.fromNamespaceAndPath("botania", "dreamwood_wand"))
                   .addTag(ItemTags.COMPASSES)
                   .addTag(commonTag("wrenches"))
                   .addTag(commonTag("shears"))
                   .addTag(commonTag("bows"))
                   .addTag(commonTag("fishing_rods"))
                   .addTag(tag("botania", "rods"))
                   .addTag(tag("hexcasting", "staves"))

                   .lang("Valid Tool Container Tools")
                   .register()
                   
       */
