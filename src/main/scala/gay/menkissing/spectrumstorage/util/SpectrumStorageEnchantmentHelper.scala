package gay.menkissing.spectrumstorage.util

import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.{Enchantment, EnchantmentHelper, ItemEnchantments}

import java.util.Optional

import scala.jdk.CollectionConverters.*

object SpectrumStorageEnchantmentHelper:
  def getRegistry(lookup: HolderLookup.Provider): Optional[HolderLookup.RegistryLookup[Enchantment]] =
    lookup.lookup(Registries.ENCHANTMENT)

  def getLevel(registryLookup: HolderLookup.Provider, enchantment: ResourceKey[Enchantment], stack: ItemStack): Int =
    getRegistry(registryLookup)
      .flatMap(_.get(enchantment))
      .map(entry => EnchantmentHelper.getItemEnchantmentLevel(entry, stack))
      .orElse(0)

  def getLevelExpensive(key: ResourceKey[Enchantment], stack: ItemStack): Int =
    stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
         .entrySet()
         .iterator()
         .asScala
         .find(_.getKey.is(key))
         .map(_.getIntValue)
         .getOrElse(0)

