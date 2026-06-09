package gay.menkissing.spectrumstorage.registries

import gay.menkissing.spectrumstorage.SpectrumStorage
import gay.menkissing.spectrumstorage.content.{SpectrumStorageBlocks, SpectrumStorageItems}
import gay.menkissing.spectrumstorage.util.SpectrumStorageNumberFormatting
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.registries.{DeferredBlock, DeferredItem}

object SpectrumStorageTranslationKeys:
  object keys:
    object bottomlessBottle:
      object tooltip:
        val usagePickup: String = SpectrumStorageItems.bottomlessBottle.get().getDescriptionId + ".tooltip.usage_pickup"
        val usagePlace: String = SpectrumStorageItems.bottomlessBottle.get().getDescriptionId + ".tooltip.usage_place"
        
        val empty: String = SpectrumStorageItems.bottomlessBottle.get().getDescriptionId + ".tooltip.empty"
        
        val countMB: String = SpectrumStorageItems.bottomlessBottle.get().getDescriptionId + ".tooltip.count_mb"

    object container:
      def make(name: String): String =
        s"container.${SpectrumStorage.ModId}.$name"

      def make(id: ResourceLocation): String =
        s"container.${id.getNamespace}.${id.getPath}"

      def make(holder: DeferredBlock[?]): String =
        val id = holder.getId
        make(id)

      def make(holder: DeferredItem[?]): String =
        make(holder.getId)

      val bottomlessAmphora: String = make(SpectrumStorageBlocks.bottomlessAmphora)
      val filterChest: String = make(SpectrumStorageBlocks.filterChest)
      val bottomlessBarrel: String = make(SpectrumStorageBlocks.bottomlessBarrel)
  
    val addedByPastelStorage: String = "book.pastelstorage.added_by_pastelstorage"
  
  object container:
    val bottomlessAmphora: Component = Component.translatable(keys.container.bottomlessAmphora)
    val filterChest: Component = Component.translatable(keys.container.filterChest)
    val bottomlessBarrel: Component = Component.translatable(keys.container.bottomlessBarrel)

  object bottomlessBottle:
    object tooltip:
      val usagePickup: Component = Component.translatable(keys.bottomlessBottle.tooltip.usagePickup)
      val usagePlace: Component = Component.translatable(keys.bottomlessBottle.tooltip.usagePlace)
      
      val empty: Component = Component.translatable(keys.bottomlessBottle.tooltip.empty)
      def countMB(amount: Int, max: Int): Component =
        Component.translatable(keys.bottomlessBottle.tooltip.countMB, SpectrumStorageNumberFormatting.formatMB(amount), SpectrumStorageNumberFormatting.formatFluidMax(max))
        
