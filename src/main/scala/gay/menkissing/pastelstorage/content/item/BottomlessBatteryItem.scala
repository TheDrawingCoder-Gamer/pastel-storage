package gay.menkissing.pastelstorage.content.item

import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.registries.{PastelStorageComponents, PastelStorageTranslationKeys}
import gay.menkissing.pastelstorage.util.PastelStorageEnchantmentHelper
import net.minecraft.core.{Holder, HolderLookup}
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item.TooltipContext
import net.minecraft.world.item.enchantment.{Enchantment, Enchantments}
import net.minecraft.world.item.{Item, ItemStack, TooltipFlag}
import net.minecraft.world.level.Level
import net.neoforged.neoforge.energy.ComponentEnergyStorage

import java.util


final class BottomlessBatteryItem(props: Item.Properties) extends Item(props):
  override def isEnchantable(stack: ItemStack): Boolean = stack.getCount == 1

  override def getEnchantmentValue(stack: ItemStack): Int = 5

  override def supportsEnchantment(stack: ItemStack, enchantment: Holder[Enchantment]): Boolean =
    super.supportsEnchantment(stack, enchantment) || enchantment.is(Enchantments.POWER)

  override def appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: util.List[Component], tooltipFlag: TooltipFlag): Unit =
    val amount = stack.getOrDefault(PastelStorageComponents.BottomlessBatteryEnergyComponent, Integer.valueOf(0)).toInt
    val max = BottomlessBatteryItem.getMaxStackRegistry(context.registries(), stack)
    tooltipComponents
      .add(PastelStorageTranslationKeys.bottomlessBattery.tooltip.countFE(amount, max))
object BottomlessBatteryItem:
  val baseMax: Int = 2_000_000

  def getStorage(stack: ItemStack): ComponentEnergyStorage =
    val max = getMaxStack(stack)
    ComponentEnergyStorage(stack, PastelStorageComponents.BottomlessBatteryEnergyComponent.get(), max)

  def getStorage(lookup: HolderLookup.Provider, stack: ItemStack): ComponentEnergyStorage =
    val max = getMaxStackRegistry(lookup, stack)
    ComponentEnergyStorage(stack, PastelStorageComponents.BottomlessBatteryEnergyComponent.get(), max)

  // getting buffed in 26.1
  def maxAllowed(level: Int): Int =
    baseMax * math.pow(4, math.min(level, 5)).toInt

  def getMaxStackRegistry(lookup: HolderLookup.Provider, stack: ItemStack): Int =
    maxAllowed(PastelStorageEnchantmentHelper.getLevel(lookup, Enchantments.POWER, stack))

  def getMaxStackLevel(level: Level, stack: ItemStack): Int =
    getMaxStackRegistry(level.registryAccess(), stack)
  def getMaxStack(stack: ItemStack): Int =
    val access = PastelStorage.getRegistryAccess
    if access == null then
      baseMax
    else
      getMaxStackRegistry(access, stack)
