package gay.menkissing.pastelstorage.content.block.entity

import gay.menkissing.pastelstorage.content.PastelStorageBlocks
import gay.menkissing.pastelstorage.content.block.BottomlessWormBlock
import gay.menkissing.pastelstorage.content.block.entity.BottomlessStorageBlockEntity.TItemStorages
import gay.menkissing.pastelstorage.content.block.entity.bottomless_storage.{BLEnergyStorage, FluidStorages}
import gay.menkissing.pastelstorage.registries.PastelStorageTranslationKeys
import gay.menkissing.pastelstorage.screen.BottomlessWormMenu
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.energy.IEnergyStorage
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler
import net.neoforged.neoforge.items.IItemHandler

import scala.jdk.OptionConverters.*

class BottomlessWormBlockEntity(pos: BlockPos, state: BlockState) extends WithoutOpenContainerBottomlessStorageBlockEntity(9, PastelStorageBlocks.bottomlessWormBlockEntity.get(), pos, state):
  override val itemStorage: TItemStorages = new WormItemStorages
  override val fluidStorage: FluidStorages = new BottomlessWormBlockEntity.WormFluidStorages(this)
  override val energyStorage: BLEnergyStorage = new BottomlessWormBlockEntity.WormEnergyStorage(this)

  // internal variable for preventing looping sadness
  var isLocked: Boolean = false

  def withLock[T](block: => T): T =
    isLocked = true
    val res = block
    isLocked = false
    res

  def getFacing: Direction =
    getBlockState.getValue(BottomlessWormBlock.FACING)

  def getNextPos: BlockPos =
    val facing = getBlockState.getValue(BottomlessWormBlock.FACING)
    getBlockPos.relative(facing)

  def getNextBS: Option[BottomlessStorageBlockEntity] =
    if this.isLooping then
      None
    else
      getLevel.getBlockEntity(getNextPos) match
        case bsbe: BottomlessStorageBlockEntity => Some(bsbe)
        case _ => None

  def withNextBS[T](f: BottomlessStorageBlockEntity => T): Option[T] =
    getNextBS.map: bs =>
      withLock(f(bs))

  def isLooping: Boolean =
    val facing = getFacing
    val nextPos = getBlockPos.relative(facing)
    val entity = getLevel.getBlockEntity(nextPos, PastelStorageBlocks.bottomlessWormBlockEntity.get())

    entity.map(_.isLocked).orElse(false)
  final class WormItemStorages extends TItemStorages:
    override def parent: BottomlessWormBlockEntity = BottomlessWormBlockEntity.this

    def getNext: Option[IItemHandler] =
      if parent.isLooping then
        None
      else
        val facing = getBlockState.getValue(BottomlessWormBlock.FACING)
        val nextPos = pos.relative(facing)
        Option(Capabilities.ItemHandler.BLOCK.getCapability(getLevel, nextPos, null, null, facing.getOpposite))

    def withNext[T](default: => T)(f: IItemHandler => T): T =
      getNext.map: handler =>
        parent.withLock(f(handler))
      .getOrElse(default)

    override def getSlots: Int =
      super.getSlots + withNext(0)(_.getSlots)

    override def getSlotLimit(slot: Int): Int =
      if slot < capacity then
        super.getSlotLimit(slot)
      else
        withNext(0)(_.getSlotLimit(slot - capacity))

    override def getStackInSlot(slot: Int): ItemStack =
      if slot < capacity then
        super.getStackInSlot(slot)
      else
        withNext(ItemStack.EMPTY)(_.getStackInSlot(slot - capacity))

    override def insertItem(slot: Int, stack: ItemStack, simulated: Boolean): ItemStack =
      if slot < capacity then
        super.insertItem(slot, stack, simulated)
      else
        withNext(stack)(_.insertItem(slot - capacity, stack, simulated))

    override def extractItem(slot: Int, amount: Int, simulated: Boolean): ItemStack =
      if slot < capacity then
        super.extractItem(slot, amount, simulated)
      else
        withNext(ItemStack.EMPTY)(_.extractItem(slot - capacity, amount, simulated))


  override def defaultName: Component = PastelStorageTranslationKeys.container.bottomlessWorm

  override def createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu =
    BottomlessWormMenu.server(i, inventory, containerView)

object BottomlessWormBlockEntity:
  class WormEnergyStorage(override val parent: BottomlessWormBlockEntity) extends BLEnergyStorage(parent):
    def nextPos: BlockPos =
      val facing = parent.getBlockState.getValue(BottomlessWormBlock.FACING)
      parent.getBlockPos.relative(facing)

    def getNext: Option[IEnergyStorage] =
      if parent.isLooping then
        None
      else
        val facing = parent.getBlockState.getValue(BottomlessWormBlock.FACING)
        Option(Capabilities.EnergyStorage.BLOCK.getCapability(parent.getLevel, nextPos, null, null, facing.getOpposite))

    def withNext[T](f: IEnergyStorage => T): Option[T] =
      getNext.map: storage =>
        parent.withLock(f(storage))


    override def energyStoredLong: Long =
      super.energyStoredLong +
        parent.withNextBS(_.energyStorage.energyStoredLong)
              .orElse(withNext(_.getEnergyStored.toLong))
              .getOrElse(0L)

    override def capacityLong: Long =
      super.capacityLong +
        parent.withNextBS(_.energyStorage.capacityLong)
                   .orElse(withNext(_.getMaxEnergyStored.toLong))
                   .getOrElse(0L)

    override def receiveEnergy(toReceive: Int, simulate: Boolean): Int =
      val received = super.receiveEnergy(toReceive, simulate)

      if received < toReceive then
        withNext(_.receiveEnergy(toReceive - received, simulate)).getOrElse(0) + received
      else
        received

    override def extractEnergy(toExtract: Int, simulated: Boolean): Int =
      val extracted = super.extractEnergy(toExtract, simulated)
      if extracted < toExtract then
        withNext(_.extractEnergy(toExtract - extracted, simulated)).getOrElse(0) + extracted
      else
        extracted


  class WormFluidStorages(override val parent: BottomlessWormBlockEntity) extends FluidStorages(parent):
    def getNext: Option[IFluidHandler] =
      if parent.isLooping then
        None
      else
        val facing = parent.getBlockState.getValue(BottomlessWormBlock.FACING)
        val nextPos = parent.getBlockPos.relative(facing)
        Option(Capabilities.FluidHandler.BLOCK.getCapability(parent.getLevel, nextPos, null, null, facing.getOpposite))

    def withNext[T](f: IFluidHandler => T): Option[T] =
      getNext.map(it => parent.withLock(f(it)))

    override def getTanks: Int =
      super.getTanks + withNext(_.getTanks).getOrElse(0)

    override def getFluidInTank(slot: Int): FluidStack =
      if slot < parent.capacity then
        super.getFluidInTank(slot)
      else
        withNext(_.getFluidInTank(slot - parent.capacity)).getOrElse(FluidStack.EMPTY)

    override def getTankCapacity(slot: Int): Int =
      if slot < parent.capacity then
        super.getTankCapacity(slot)
      else
        withNext(_.getTankCapacity(slot - parent.capacity)).getOrElse(0)

    // try it!
    override def isFluidValid(slot: Int, fluidStack: FluidStack): Boolean = true

    override def fill(resource: FluidStack, fluidAction: IFluidHandler.FluidAction): Int =
      val amountFilled = super.fill(resource, fluidAction)
      if amountFilled < resource.getAmount then
        val nextStack = resource.copyWithAmount(resource.getAmount - amountFilled)
        withNext(_.fill(nextStack, fluidAction)).getOrElse(0) + amountFilled
      else
        amountFilled

    override def drain(resource: FluidStack, fluidAction: IFluidHandler.FluidAction): FluidStack =
      val drainedStack = super.drain(resource, fluidAction)
      if drainedStack.getAmount < resource.getAmount then
        val remainderStack = resource.copyWithAmount(resource.getAmount - drainedStack.getAmount)
        val res = withNext(_.drain(remainderStack, fluidAction)).getOrElse(FluidStack.EMPTY)
        drainedStack.grow(res.getAmount)
      drainedStack

    override def drain(maxDrain: Int, fluidAction: IFluidHandler.FluidAction): FluidStack =
      val drainedStack = super.drain(maxDrain, fluidAction)
      if drainedStack.isEmpty then
        withNext(_.drain(maxDrain, fluidAction)).getOrElse(FluidStack.EMPTY)
      else
        if drainedStack.getAmount < maxDrain then
          val remainderStack = drainedStack.copyWithAmount(maxDrain - drainedStack.getAmount)
          val res = withNext(_.drain(remainderStack, fluidAction)).getOrElse(FluidStack.EMPTY)
          drainedStack.grow(res.getAmount)
        drainedStack