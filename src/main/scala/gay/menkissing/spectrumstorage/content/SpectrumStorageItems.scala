package gay.menkissing.spectrumstorage.content

import earth.terrarium.pastel.helpers.enchantments.Ench
import gay.menkissing.spectrumstorage.SpectrumStorage
import gay.menkissing.spectrumstorage.content.item.{BottomlessBottleItem, ToolContainerItem}
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.{CreativeModeTab, Item, ItemStack}
import earth.terrarium.pastel.registries.{PastelBlocks, PastelItemGroups}
import gay.menkissing.spectrumstorage.content.SpectrumStorageBlocks.blockItems
import gay.menkissing.spectrumstorage.registries.SpectrumStorageComponents
import gay.menkissing.spectrumstorage.util.{FluidConverter, SpectrumStorageEnchantmentHelper}
import net.minecraft.world.item.enchantment.Enchantments
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.capabilities.{Capabilities, RegisterCapabilitiesEvent}
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack
import net.neoforged.neoforge.registries.{DeferredItem, DeferredRegister, RegisterEvent}
import org.sinytra.fabric.transfer_api.compat.FluidStorageFluidHandlerItem

import scala.collection.mutable
import scala.jdk.CollectionConverters.*

object SpectrumStorageItems:
  val registrar: DeferredRegister.Items = DeferredRegister.createItems(SpectrumStorage.ModId)
  
  def register[T <: Item](name: String, item: => T): DeferredItem[T] =
    registrar.register(name, () => item)

  val bottomlessBottle: DeferredItem[Item] =
    register("bottomless_bottle", new BottomlessBottleItem(Item.Properties().stacksTo(1)))

  val toolContainer: DeferredItem[Item] =
    register("tool_container", new ToolContainerItem(Item.Properties().stacksTo(1)))

  def submit(bus: IEventBus): Unit =
    // TODO IMPORTANT!

    registrar.register(bus)
    bus.addListener: (ev: FMLCommonSetupEvent) =>
      BottomlessBottleItem.registerCauldronInteractions()
    bus.addListener: (ev: BuildCreativeModeTabContentsEvent) =>
      if ev.getTabKey.location() == PastelItemGroups.TOOLS_ID then
        val baseStack = bottomlessBottle.toStack(1)
        ev.insertAfter(PastelBlocks.BOTTOMLESS_BUNDLE.toStack(1), baseStack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS)
        ev.insertAfter(baseStack, toolContainer.toStack(1), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS)
    bus.addListener: (it: RegisterCapabilitiesEvent) =>
      it.registerItem(
        Capabilities.FluidHandler.ITEM,
        (stack, _) => {
          FluidHandlerItemStack(SpectrumStorageComponents.BottomlessBottleContentsComponent, stack, FluidConverter.dropletToMb(BottomlessBottleItem.getMaxStack(stack)))
        },
        bottomlessBottle.get()
      )

    // LATER:tm:
    /*
    bus.addListener: (ev: RegisterCapabilitiesEvent) =>
      ev.registerItem(
        Capabilities.FluidHandler.ITEM,
        (stack, ctx) => BottomlessBottleItem.BottomlessBottleContents.BottomlessBottleStorage(())
      )

     */
