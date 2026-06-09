package gay.menkissing.spectrumstorage.content.item

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.item.{Item, ItemDisplayContext, ItemStack, TooltipFlag}
import gay.menkissing.spectrumstorage.SpectrumStorage
import gay.menkissing.spectrumstorage.content.SpectrumStorageItems
import gay.menkissing.spectrumstorage.registries.{SpectrumStorageComponents, SpectrumStorageTranslationKeys}
import gay.menkissing.spectrumstorage.util.{FluidResource, SpectrumStorageEnchantmentHelper}
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
    val contents = stack.getOrDefault(SpectrumStorageComponents.BottomlessBottleContentsComponent, SimpleFluidContent.EMPTY)
    if contents.isEmpty then
      tooltipComponents.add(SpectrumStorageTranslationKeys.bottomlessBottle.tooltip.empty)
      tooltipComponents.add(SpectrumStorageTranslationKeys.bottomlessBottle.tooltip.usagePickup)
    else
      val containedFluid = contents.getFluidType
      val maxDroplets = BottomlessBottleItem.getMaxStackRegistry(context.registries(), stack)
      tooltipComponents.add(SpectrumStorageTranslationKeys.bottomlessBottle.tooltip.countMB(contents.getAmount, maxDroplets))
      tooltipComponents.add(containedFluid.getDescription)
      tooltipComponents.add(SpectrumStorageTranslationKeys.bottomlessBottle.tooltip.usagePickup)
      tooltipComponents.add(SpectrumStorageTranslationKeys.bottomlessBottle.tooltip.usagePlace)

  def playEmptyingSound(player: Player, level: Level, pos: BlockPos, fluidType: FluidType): Unit =
    val sound = fluidType.getSound(player, level, pos, SoundActions.BUCKET_EMPTY)
    if sound != null then
      level.playSound(player, pos, sound, SoundSource.BLOCKS, 1f, 1f)

  def placeFluid(player: Player | Null, level: Level, pos: BlockPos, hitResult: BlockHitResult, thisStack: ItemStack): Boolean =
    val contents = thisStack.get(SpectrumStorageComponents.BottomlessBottleContentsComponent)
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
    val contents = stack.get(SpectrumStorageComponents.BottomlessBottleContentsComponent)
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
          val builder = BottomlessBottleItem.SimpleFluidContentBuilder.fromStack(stack)
          if player.isShiftKeyDown then
            // placing
            if builder.extract(builder.template, FluidType.BUCKET_VOLUME) != FluidType.BUCKET_VOLUME then
              return InteractionResultHolder.fail(stack)

            val targetPos = if hitState.getBlock.isInstanceOf[LiquidBlockContainer] then hitPos else placePos
            if this.placeFluid(player, level, targetPos, blockHitResult, stack) then
              if player.getAbilities.instabuild then
                return InteractionResultHolder.success(stack)

              val newStack = stack.copy()
              builder.buildAndSet(newStack)
              return InteractionResultHolder.success(newStack)
          else
            // pickup
            if builder.max - builder.amount >= FluidType.BUCKET_VOLUME then
              val fluid = level.getFluidState(hitPos)

              if fluid != null && (builder.isEmpty || builder.template.fluid == fluid.getType) then
                if builder.insert(FluidResource.of(fluid.getType), FluidType.BUCKET_VOLUME) == FluidType.BUCKET_VOLUME then
                  hitState.getBlock match
                    case bucketPickup: BucketPickup =>
                      if !bucketPickup.pickupBlock(player, level, hitPos, hitState).isEmpty then
                        val sound = fluid.getFluidType.getSound(player, level, hitPos, SoundActions.BUCKET_FILL)
                        if sound != null then
                          level.playSound(player, hitPos, sound, SoundSource.BLOCKS, 1f, 1f)

                        val newStack = stack.copy()
                        builder.buildAndSet(newStack)
                        return InteractionResultHolder.success(newStack)
                    case _ => ()
        InteractionResultHolder.fail(stack)

object BottomlessBottleItem:
  val baseMax: Int = FluidType.BUCKET_VOLUME * 256

  @nowarn("msg=eta")
  def registerCauldronInteractions(): Unit =
    CauldronInteraction.EMPTY.map().put(SpectrumStorageItems.bottomlessBottle.get(), emptyBottleInteraction)
    CauldronInteraction.LAVA.map().put(SpectrumStorageItems.bottomlessBottle.get(), fillBottleInteraction(Fluids.LAVA))
    CauldronInteraction.WATER.map().put(SpectrumStorageItems.bottomlessBottle.get(), fillBottleInteraction(Fluids.WATER))


  def maxAllowed(level: Int): Int =
    // Have to lower this due to long shenanagins
    baseMax * math.pow(4, math.min(level, 5)).toInt
  def getMaxStack(world: Level, stack: ItemStack): Int =
    getMaxStackRegistry(world.registryAccess(), stack)
  def getMaxStackRegistry(lookup: HolderLookup.Provider, stack: ItemStack): Int =
    maxAllowed(SpectrumStorageEnchantmentHelper.getLevel(lookup, Enchantments.POWER, stack))
  def getMaxStack(stack: ItemStack): Int =
    val access = SpectrumStorage.getRegistryAccess
    if access == null then
      baseMax
    else
      getMaxStackRegistry(access, stack)

  def emptyBottleInteraction(blockState: BlockState, level: Level, blockPos: BlockPos, player: Player, usedHand: InteractionHand, stack: ItemStack): ItemInteractionResult =
    val contents = SimpleFluidContentBuilder.fromStack(stack)

    if contents.template.fluid == Fluids.WATER then
      if contents.extract(FluidResource.of(Fluids.WATER), FluidType.BUCKET_VOLUME) == FluidType.BUCKET_VOLUME then
        level.setBlockAndUpdate(blockPos, Blocks.WATER_CAULDRON.defaultBlockState()
          .setValue(LayeredCauldronBlock.LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL))
        level.playSound(player, blockPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1f, 1f)
        contents.buildAndSet(stack)
        return ItemInteractionResult.SUCCESS
    else if contents.template.fluid == Fluids.LAVA then
      if contents.extract(FluidResource.of(Fluids.LAVA), FluidType.BUCKET_VOLUME) == FluidType.BUCKET_VOLUME then
        level.setBlockAndUpdate(blockPos, Blocks.LAVA_CAULDRON.defaultBlockState())
        level.playSound(player, blockPos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 1f, 1f)
        contents.buildAndSet(stack)
        return ItemInteractionResult.SUCCESS
    ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION

  def fillBottleInteraction(fluid: Fluid)(blockState: BlockState, level: Level, blockPos: BlockPos, player: Player, usedHand: InteractionHand, stack: ItemStack): ItemInteractionResult =
    val contents = SimpleFluidContentBuilder.fromStack(stack)
    if contents.isEmpty || contents.template.fluid.isSame(fluid) then
      // Thou shalt convert thine Integer to Int unless thy want sadness
      val amount = blockState.getOptionalValue(LayeredCauldronBlock.LEVEL).orElse(3).toInt
      if amount != 3 then
        // Fail here because FORGE USES IMPRECISE MB!
        // and my ass is NOT rounding
        ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
      else if contents.insert(FluidResource.of(fluid), FluidType.BUCKET_VOLUME) == FluidType.BUCKET_VOLUME then
        contents.buildAndSet(stack)
        level.setBlockAndUpdate(blockPos, Blocks.CAULDRON.defaultBlockState())
        val emptySound = contents.template.fluid.getFluidType.getSound(player, level, blockPos, SoundActions.BUCKET_EMPTY)
        if emptySound != null then
          level.playSound(player, blockPos, emptySound, SoundSource
            .BLOCKS, 1f, 1f)
        ItemInteractionResult.SUCCESS
      else
        ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
    else
      ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
  


  // Max MUST be long due to our maximum possible being outside the int range
  final class SimpleFluidContentBuilder(var template: FluidResource, var amount: Int, var max: Int):
    def isEmpty: Boolean =
      template.isBlank || amount == 0

    def result(): SimpleFluidContent =
      SimpleFluidContent.copyOf(FluidStack(template.fluid.builtInRegistryHolder(), amount, template.components))

    def buildAndSet(stack: ItemStack): Unit =
      val res = result()
      stack.set(SpectrumStorageComponents.BottomlessBottleContentsComponent, res)

    def getMaxAllowed(variant: FluidResource, amount: Int): Int =
      if variant.isBlank || amount <= 0 || (!this.isEmpty && template != variant) then
        0
      else
        val r = this.max - this.amount
        if r > Int.MaxValue then Int.MaxValue else r

    def insert(variant: FluidResource, amount: Int): Int =
      val added = math.min(amount, getMaxAllowed(variant, amount))
      if added == 0 then
        return 0
      if this.isEmpty then
        this.template = variant

      this.amount += math.min(this.max - this.amount, added)
      added

    def extract(variant: FluidResource, amount: Int): Int =
      if template != variant then
        0
      else
        val toRemove = math.min(this.amount, amount)
        this.amount -= toRemove
        if this.amount == 0 then
          this.template = FluidResource.EMPTY

        toRemove
  object SimpleFluidContentBuilder:
    def fromStack(stack: ItemStack): SimpleFluidContentBuilder =
      val content = stack.getOrDefault(SpectrumStorageComponents.BottomlessBottleContentsComponent, SimpleFluidContent.EMPTY)
      val resource = FluidResource.ofStack(content.copy())
      SimpleFluidContentBuilder(resource, content.getAmount, getMaxStack(stack))

