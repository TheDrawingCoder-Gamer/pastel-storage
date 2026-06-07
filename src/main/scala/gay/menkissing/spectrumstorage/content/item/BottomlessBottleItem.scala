package gay.menkissing.spectrumstorage.content.item

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.item.{Item, ItemDisplayContext, ItemStack, TooltipFlag}
import gay.menkissing.spectrumstorage.SpectrumStorage
import gay.menkissing.spectrumstorage.content.SpectrumStorageItems
import gay.menkissing.spectrumstorage.registries.{LumoComponents, LumoTranslationKeys}
import gay.menkissing.spectrumstorage.util.{FabricJankinator, LumoEnchantmentHelper}
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.{FluidConstants, FluidVariant, FluidVariantAttributes}
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.core.{BlockPos, Direction, Holder, HolderLookup}
import net.minecraft.core.cauldron.CauldronInteraction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.{ByteBufCodecs, StreamCodec}
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.{SoundEvents, SoundSource}
import net.minecraft.tags.FluidTags
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.item.Item.TooltipContext
import net.minecraft.world.{InteractionHand, InteractionResult, InteractionResultHolder, ItemInteractionResult}
import net.minecraft.world.item.enchantment.{Enchantment, EnchantmentHelper, Enchantments}
import net.minecraft.world.level.{BlockAndTintGetter, ClipContext, Level}
import net.minecraft.world.level.block.{Blocks, BucketPickup, LayeredCauldronBlock, LiquidBlockContainer}
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.{Fluid, Fluids}
import net.minecraft.world.phys.{BlockHitResult, HitResult}
import net.neoforged.neoforge.client.model.IDynamicBakedModel
import net.neoforged.neoforge.common.SoundActions
import net.neoforged.neoforge.fluids.{FluidStack, FluidType, SimpleFluidContent}

import java.util
import java.util.function
import java.util.function.Supplier
import scala.annotation.nowarn

class BottomlessBottleItem(props: Item.Properties) extends Item(props):
  override def isEnchantable(stack: ItemStack): Boolean = true

  override def getEnchantmentValue: Int = 5

  override def supportsEnchantment(stack: ItemStack, enchantment: Holder[Enchantment]): Boolean =
    super.supportsEnchantment(stack, enchantment) || enchantment.is(Enchantments.POWER)


  override def appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: util.List[Component], tooltipFlag: TooltipFlag): Unit =
    val contents = BottomlessBottleItem.BottomlessBottleContents.getFromStack(stack)
    if contents.isEmpty then
      tooltipComponents.add(LumoTranslationKeys.bottomlessBottle.tooltip.empty)
      tooltipComponents.add(LumoTranslationKeys.bottomlessBottle.tooltip.usagePickup)
    else
      val containedFluid = contents.variant
      val max = BottomlessBottleItem.getMaxStackRegistry(context.registries(), stack)
      tooltipComponents.add(LumoTranslationKeys.bottomlessBottle.tooltip.countMB(contents.amount, max))
      tooltipComponents.add(FluidVariantAttributes.getName(containedFluid))
      tooltipComponents.add(LumoTranslationKeys.bottomlessBottle.tooltip.usagePickup)
      tooltipComponents.add(LumoTranslationKeys.bottomlessBottle.tooltip.usagePlace)

  def playEmptyingSound(player: Player, level: Level, pos: BlockPos, fluidType: FluidType): Unit =
    val sound = fluidType.getSound(player, level, pos, SoundActions.BUCKET_EMPTY)
    if sound != null then
      level.playSound(player, pos, sound, SoundSource.BLOCKS, 1f, 1f)

  def placeFluid(player: Player | Null, level: Level, pos: BlockPos, hitResult: BlockHitResult, thisStack: ItemStack): Boolean =
    val contents = thisStack.get(LumoComponents.BottomlessBottleContentsComponent)
    if contents.isEmpty || contents.getAmount < FluidType.BUCKET_VOLUME then
      return false

    val blockState = level.getBlockState(pos)
    val canPlace = blockState.canBeReplaced(contents.getFluid)

    if
      !blockState.isAir && !canPlace && (
        blockState.getBlock match
          case liquidBlock: LiquidBlockContainer => !liquidBlock.canPlaceLiquid(player, level, pos, blockState, contents.getFluid)
          case _ => true
      )
    then
      hitResult != null && this.placeFluid(player, level, hitResult.getBlockPos.relative(hitResult.getDirection), null, thisStack)
    else
      if level.dimensionType().ultraWarm() && contents.getFluid.is(FluidTags.WATER) then
        val i = pos.getX
        val j = pos.getY
        val k = pos.getZ
        level.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.6f + (level.random.nextFloat() - level.random.nextFloat()) * 0.8f)
        (0 until 8).foreach: _ =>
          level.addParticle(ParticleTypes.LARGE_SMOKE, i.toDouble + math.random(), j.toDouble + math.random(), k.toDouble + math.random(), 0d, 0d, 0d)
      else
        blockState.getBlock match
          case liquidBlockContainer: LiquidBlockContainer if contents.getFluid == Fluids.WATER =>
            if liquidBlockContainer.placeLiquid(level, pos, blockState, Fluids.WATER.getSource(false)) then
              this.playEmptyingSound(player, level, pos, contents.getFluidType)
          case _ =>
            if !level.isClientSide && canPlace && !blockState.liquid() then
              level.removeBlock(pos, true)

            this.playEmptyingSound(player, level, pos, contents.getFluidType)
            level.setBlock(pos, contents.getFluid.defaultFluidState().createLegacyBlock(), 11)
      true

  override def use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder[ItemStack] =
    val stack = player.getItemInHand(usedHand)
    val contents = stack.get(LumoComponents.BottomlessBottleContentsComponent)
    val blockHitResult = Item.getPlayerPOVHitResult(level, player, if player.isShiftKeyDown then ClipContext.Fluid.NONE else ClipContext.Fluid.SOURCE_ONLY)
    blockHitResult.getType match
      case HitResult.Type.MISS | HitResult.Type.ENTITY =>
        InteractionResultHolder.pass(stack)
      case HitResult.Type.BLOCK =>
        val hitPos = blockHitResult.getBlockPos
        val direction = blockHitResult.getDirection
        val placePos = hitPos.relative(direction)
        if !level.mayInteract(player, hitPos) || !player.mayUseItemAt(placePos, direction, stack) then
          InteractionResultHolder.fail(stack)
        else
          val hitState = level.getBlockState(hitPos)
          val builder = BottomlessBottleItem.BottomlessBottleContents.Builder.of(level, stack)
          if player.isShiftKeyDown then
            // placing
            if builder.extract(builder.template, FluidConstants.BUCKET) != FluidConstants.BUCKET then
              return InteractionResultHolder.fail(stack)

            val targetPos = if hitState.getBlock.isInstanceOf[LiquidBlockContainer] then hitPos else placePos
            if this.placeFluid(player, level, targetPos, blockHitResult, stack) then
              if player.getAbilities.instabuild then
                return InteractionResultHolder.success(stack)

              val newStack = stack.copy()
              BottomlessBottleItem.BottomlessBottleContents.replaceInStack(newStack, builder.build)
              return InteractionResultHolder.success(newStack)
          else
            // pickup
            if builder.max - builder.amount >= FluidConstants.BUCKET then
              val fluid = level.getFluidState(hitPos)

              if fluid != null && (builder.isEmpty || builder.template.getFluid == fluid.getType) then
                if builder.insert(FluidVariant.of(fluid.getType), FluidConstants.BUCKET) == FluidConstants.BUCKET then
                  hitState.getBlock match
                    case bucketPickup: BucketPickup =>
                      if !bucketPickup.pickupBlock(player, level, hitPos, hitState).isEmpty then
                        val sound = FluidVariantAttributes.getFillSound(FluidVariant.of(fluid.getType))
                        level.playSound(player, hitPos, sound, SoundSource.BLOCKS, 1f, 1f)

                        val newStack = stack.copy()
                        BottomlessBottleItem.BottomlessBottleContents.replaceInStack(newStack, builder.build)
                        return InteractionResultHolder.success(newStack)
                    case _ => ()
        InteractionResultHolder.fail(stack)

object BottomlessBottleItem:
  val baseMax: Long = FluidConstants.BUCKET * 256

  /*
  @nowarn("msg=eta")
  def registerCauldronInteractions(): Unit =
    CauldronInteraction.EMPTY.map().put(SpectrumStorageItems.bottomlessBottle.get(), emptyBottleInteraction)
    CauldronInteraction.LAVA.map().put(SpectrumStorageItems.bottomlessBottle.get(), fillBottleInteraction(Fluids.LAVA))
    CauldronInteraction.WATER.map().put(SpectrumStorageItems.bottomlessBottle.get(), fillBottleInteraction(Fluids.WATER))
  */

  def maxAllowed(level: Int): Long =
    baseMax * math.pow(8, math.min(level, 5)).toLong
  def getMaxStack(world: Level, stack: ItemStack): Long =
    getMaxStackRegistry(world.registryAccess(), stack)
  def getMaxStackRegistry(lookup: HolderLookup.Provider, stack: ItemStack): Long =
    maxAllowed(LumoEnchantmentHelper.getLevel(lookup, Enchantments.POWER, stack))
  // Not really that expensive on 1.20, but 1.21 makes this expensive
  def getMaxStackExpensive(stack: ItemStack): Long =
    maxAllowed(LumoEnchantmentHelper.getLevelExpensive(Enchantments.POWER, stack))
  /*
  def emptyBottleInteraction(blockState: BlockState, level: Level, blockPos: BlockPos, player: Player, usedHand: InteractionHand, stack: ItemStack): ItemInteractionResult =
    val contents = BottomlessBottleContents.getFromStack(stack)

    if contents.variant.getFluid == Fluids.WATER then
      val builder = BottomlessBottleContents.Builder.of(level, stack)
      if builder.extract(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET) == FluidConstants.BUCKET then
        level.setBlockAndUpdate(blockPos, Blocks.WATER_CAULDRON.defaultBlockState()
          .setValue(LayeredCauldronBlock.LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL))
        level.playSound(player, blockPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1f, 1f)
        BottomlessBottleContents.replaceInStack(stack, builder.build)
        return ItemInteractionResult.SUCCESS
    else if contents.variant.getFluid == Fluids.LAVA then
      val builder = BottomlessBottleContents.Builder.of(level, stack)
      if builder.extract(FluidVariant.of(Fluids.LAVA), FluidConstants.BUCKET) == FluidConstants.BUCKET then
        level.setBlockAndUpdate(blockPos, Blocks.LAVA_CAULDRON.defaultBlockState())
        level.playSound(player, blockPos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 1f, 1f)
        BottomlessBottleContents.replaceInStack(stack, builder.build)
        return ItemInteractionResult.SUCCESS
    ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION

  def fillBottleInteraction(fluid: Fluid)(blockState: BlockState, level: Level, blockPos: BlockPos, player: Player, usedHand: InteractionHand, stack: ItemStack): ItemInteractionResult =
    val contents = BottomlessBottleContents.getFromStack(stack)
    if contents.isEmpty || contents.variant.getFluid == fluid then
      val amount = blockState.getOptionalValue(LayeredCauldronBlock.LEVEL).orElse(3) * FluidConstants.BOTTLE
      val builder = BottomlessBottleContents.Builder.of(level, stack)
      if builder.insert(FluidVariant.of(fluid), amount) == amount then
        BottomlessBottleContents.replaceInStack(stack, builder.build)
        level.setBlockAndUpdate(blockPos, Blocks.CAULDRON.defaultBlockState())
        level.playSound(player, blockPos, FluidVariantAttributes.getFillSound(builder.template), SoundSource
          .BLOCKS, 1f, 1f)
        return ItemInteractionResult.SUCCESS

    ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
  */
  final case class BottomlessBottleContents(variant: FluidVariant, amount: Long):
    def isEmpty: Boolean = variant.isBlank || amount == 0

    def toSimple: SimpleFluidContent =
      val stack = FluidStack(variant.getRegistryEntry, FabricJankinator.dropletToMb(amount), variant.getComponents)
      SimpleFluidContent.copyOf(stack)

  object BottomlessBottleContents:

    val EMPTY: BottomlessBottleContents =
      BottomlessBottleContents(FluidVariant.blank(), 0)



    def getFromStack(stack: ItemStack): BottomlessBottleContents =
      val r = stack.get(LumoComponents.BottomlessBottleContentsComponent)
      if r != null then
        val it = r.copy()
        BottomlessBottleContents(FluidVariant.of(it.getFluid, it.getComponentsPatch), FabricJankinator.mbToDroplet(r.getAmount))
      else
        BottomlessBottleContents.EMPTY

    def replaceInStack(stack: ItemStack, contents: BottomlessBottleContents): Unit =
      stack.set(LumoComponents.BottomlessBottleContentsComponent, contents.toSimple)

    class Builder(var template: FluidVariant, var amount: Long, val max: Long):
      def isEmpty: Boolean =
        template.isBlank || amount == 0

      def copied: Builder =
        Builder(template, amount, max)

      def build: BottomlessBottleContents =
        BottomlessBottleContents(template, amount)

      def getMaxAllowed(variant: FluidVariant, amount: Long): Long =
        if variant.isBlank || amount <= 0 || (!this.isEmpty && template != variant) then
          0
        else
          this.max - this.amount

      def insert(variant: FluidVariant, amount: Long): Long =
        val added = math.min(amount, getMaxAllowed(variant, amount))
        if added == 0 then
          return 0
        if this.isEmpty then
          this.template = variant

        this.amount += math.min(this.max - this.amount, added)
        added

      def extract(variant: FluidVariant, amount: Long): Long =
        if variant != template then
          0
        else
          val toRemove = math.min(this.amount, amount)
          this.amount -= toRemove
          if this.amount == 0 then
            this.template = FluidVariant.blank()

          toRemove
    object Builder:
      def of(world: Level, stack: ItemStack): BottomlessBottleContents.Builder =
        val prev = getFromStack(stack)
        val max = getMaxStack(world, stack)
        BottomlessBottleContents.Builder(prev.variant, prev.amount, max)

      def withMax(stack: ItemStack, max: Long): BottomlessBottleContents.Builder =
        val prev = getFromStack(stack)
        BottomlessBottleContents.Builder(prev.variant, prev.amount, max)

    class BottomlessBottleStorage(val context: ContainerItemContext, var maxStoredInBundle: Long) extends SingleSlotStorage[FluidVariant]:
      override def getCapacity: Long =
        if !context.getItemVariant.isOf(SpectrumStorageItems.bottomlessBottle.get()) then
          0
        else
          maxStoredInBundle

      override def extract(resource: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long =
        StoragePreconditions.notBlankNotNegative(resource, maxAmount)

        if !context.getItemVariant.isOf(SpectrumStorageItems.bottomlessBottle.get()) then
          return 0

        val builder = Builder.withMax(context.getItemVariant.toStack, maxStoredInBundle)

        if !builder.isEmpty && resource == builder.template then
          val extracted = builder.extract(resource, maxAmount.toInt)
          val newStack = context.getItemVariant.toStack
          replaceInStack(newStack, builder.build)
          val newVariant = ItemVariant.of(newStack)

          if context.exchange(newVariant, 1, transaction) == 1 then
            return extracted

        0

      override def insert(resource: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long =
        StoragePreconditions.notBlankNotNegative(resource, maxAmount)

        if !context.getItemVariant.isOf(SpectrumStorageItems.bottomlessBottle.get()) then
          return 0

        val builder = Builder.withMax(context.getItemVariant.toStack, maxStoredInBundle)

        if builder.isEmpty || resource == builder.template then
          val inserted = builder.insert(resource, maxAmount)
          val newStack = context.getItemVariant.toStack
          replaceInStack(newStack, builder.build)
          val newVariant = ItemVariant.of(newStack)

          if context.exchange(newVariant, 1, transaction) == 1 then
            return inserted

        0

      override def isResourceBlank: Boolean =
        !context.getItemVariant.isOf(SpectrumStorageItems.bottomlessBottle.get())
        || getFromStack(context.getItemVariant.toStack).variant.isBlank

      override def getResource: FluidVariant =
        if !context.getItemVariant.isOf(SpectrumStorageItems.bottomlessBottle.get()) then
          FluidVariant.blank()
        else
          getFromStack(context.getItemVariant.toStack).variant

      override def getAmount: Long =
        if !context.getItemVariant.isOf(SpectrumStorageItems.bottomlessBottle.get()) then
          0
        else
          getFromStack(context.getItemVariant.toStack).amount

