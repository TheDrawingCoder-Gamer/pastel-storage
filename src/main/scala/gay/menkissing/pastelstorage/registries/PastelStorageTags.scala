package gay.menkissing.pastelstorage.registries

import gay.menkissing.pastelstorage.PastelStorage
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.{ItemTags, TagKey}
import net.minecraft.world.item.{Item, Items}

object PastelStorageTags:
  def commonTag(path: String): ResourceLocation =
    ResourceLocation.fromNamespaceAndPath("c", path)

  object item:
    private def tag(namespace: String, path: String): TagKey[Item] =
      TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path))
    private def commonTag(path: String): TagKey[Item] =
      TagKey.create(Registries.ITEM, PastelStorageTags.commonTag(path))

    val validToolTag: TagKey[Item] =
      TagKey.create(Registries.ITEM, PastelStorage.locate("valid_tools"))
    
    val deletesItemsWhenInsertedInto: TagKey[Item] =
      TagKey.create(Registries.ITEM, PastelStorage.locate("deletes_items_when_inserted_into"))
