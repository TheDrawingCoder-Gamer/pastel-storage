package gay.menkissing.spectrumstorage.registries

import gay.menkissing.spectrumstorage.SpectrumStorage
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.{ItemTags, TagKey}
import net.minecraft.world.item.{Item, Items}

object SpectrumStorageTags:
  def commonTag(path: String): ResourceLocation =
    ResourceLocation.fromNamespaceAndPath("c", path)

  object item:
    private def tag(namespace: String, path: String): TagKey[Item] =
      TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path))
    private def commonTag(path: String): TagKey[Item] =
      TagKey.create(Registries.ITEM, SpectrumStorageTags.commonTag(path))

    val validToolTag: TagKey[Item] =
      TagKey.create(Registries.ITEM, SpectrumStorage.locate("valid_tools"))
