package gay.menkissing.pastelstorage.content

import earth.terrarium.pastel.helpers.enchantments.Ench
import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.content.item.{BottomlessBatteryItem, BottomlessBottleItem, ToolContainerItem}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.{CreativeModeTab, Item, ItemStack}
import earth.terrarium.pastel.registries.{PastelBlocks, PastelItemGroups}
import gay.menkissing.pastelstorage.registries.PastelStorageComponents
import gay.menkissing.pastelstorage.util.PastelStorageEnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.capabilities.{Capabilities, RegisterCapabilitiesEvent}
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.energy.ComponentEnergyStorage
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack
import net.neoforged.neoforge.registries.{DeferredItem, DeferredRegister, RegisterEvent}

import scala.collection.mutable
import scala.jdk.CollectionConverters.*

object PastelStorageItems:
  val registrar: DeferredRegister.Items = DeferredRegister.createItems(PastelStorage.ModId)

  def register[T <: Item](name: String, item: => T): DeferredItem[T] =
    registrar.register(name, () => item)

  val bottomlessBottle: DeferredItem[Item] =
    register("bottomless_bottle", new BottomlessBottleItem(Item.Properties().stacksTo(1)))

  val bottomlessBattery: DeferredItem[Item] =
    register("bottomless_battery", new BottomlessBatteryItem(Item.Properties().stacksTo(1)))

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
        ev.insertAfter(baseStack, bottomlessBattery.toStack(1), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS)
    bus.addListener: (it: RegisterCapabilitiesEvent) =>
      it.registerItem(
        Capabilities.FluidHandler.ITEM,
        (stack, _) => {
          FluidHandlerItemStack(PastelStorageComponents.BottomlessBottleContentsComponent, stack, BottomlessBottleItem.getMaxStack(stack))
        },
        bottomlessBottle.get()
      )
      it.registerItem(
        Capabilities.EnergyStorage.ITEM,
        (stack, _) => {
          BottomlessBatteryItem.getStorage(stack)
        },
        bottomlessBattery.get()
      )