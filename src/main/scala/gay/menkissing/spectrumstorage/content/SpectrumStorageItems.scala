package gay.menkissing.spectrumstorage.content

import gay.menkissing.spectrumstorage.SpectrumStorage
import gay.menkissing.spectrumstorage.content.item.{BottomlessBottleItem, ToolContainerItem}
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.{Item, ItemStack}
import de.dafuqs.fractal.api.CreativeSubTabEvent
import de.dafuqs.spectrum.api.item_group.ItemGroupIDs
import gay.menkissing.spectrumstorage.content.SpectrumStorageBlocks.blockItems
import gay.menkissing.spectrumstorage.util.LumoEnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.capabilities.{Capabilities, RegisterCapabilitiesEvent}
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.registries.{DeferredItem, DeferredRegister, RegisterEvent}

import scala.collection.mutable
import scala.jdk.CollectionConverters.*

object SpectrumStorageItems:
  val registrar: DeferredRegister.Items = DeferredRegister.createItems(SpectrumStorage.ModId)

  private val items = mutable.Map[ResourceLocation, mutable.ArrayBuffer[DeferredItem[Item]]]()


  def register[T <: Item](name: String, tab: ResourceLocation, item: => T): DeferredItem[T] =
    val it = registrar.register(name, () => item)
    items.getOrElseUpdate(tab, mutable.ArrayBuffer.empty).addOne(it.asInstanceOf[DeferredItem[Item]])
    it

  val bottomlessBottle: DeferredItem[Item] =
    register("bottomless_bottle", ItemGroupIDs.SUBTAB_EQUIPMENT, new BottomlessBottleItem(Item.Properties().stacksTo(1)))
    /*
    InfoCollector.instance.item(SpectrumStorage.locate("bottomless_bottle"), new BottomlessBottleItem(Item.Properties().stacksTo(1)))
                 .lang("Bottomless Bottle")
                 .tooltip("empty", "Empty")
                 .tooltip("usage_pickup", "Use to pickup")
                 .tooltip("usage_place", "Sneak-use to place")
                 .tooltip("count_mb", "%1$s mB / %2$s buckets")
                 .addToSubGroup(ItemGroupIDs.SUBTAB_EQUIPMENT)

     */

  val toolContainer: DeferredItem[Item] =
    register("tool_container", ItemGroupIDs.SUBTAB_EQUIPMENT, new ToolContainerItem(Item.Properties().stacksTo(1)))
    /*
    InfoCollector.instance.item(SpectrumStorage.locate("tool_container"), new ToolContainerItem(Item.Properties().stacksTo(1)))
                 .lang("Tool Container")
                 .defaultModel()
                 .addToSubGroup(ItemGroupIDs.SUBTAB_EQUIPMENT)
     */

  def submit(bus: IEventBus): Unit =
    // TODO IMPORTANT!
    //bus.addListener: (ev: RegisterEvent) =>
    //  BottomlessBottleItem.registerCauldronInteractions()
    registrar.register(bus)
    NeoForge.EVENT_BUS.addListener[CreativeSubTabEvent]((it: CreativeSubTabEvent) => {
      val subgroup = it.subGroup()
      items.get(subgroup.getIdentifier).foreach { items =>
        val builder = it.getItemDisplayBuilder
        items.foreach(builder.accept)
      }
    })

    // LATER:tm:
    /*
    bus.addListener: (ev: RegisterCapabilitiesEvent) =>
      ev.registerItem(
        Capabilities.FluidHandler.ITEM,
        (stack, ctx) => BottomlessBottleItem.BottomlessBottleContents.BottomlessBottleStorage(())
      )

     */
