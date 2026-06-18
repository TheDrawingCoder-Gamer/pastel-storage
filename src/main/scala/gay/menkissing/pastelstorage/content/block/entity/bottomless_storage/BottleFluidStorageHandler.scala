package gay.menkissing.pastelstorage.content.block.entity.bottomless_storage

import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.api.fluid.FluidResource
import gay.menkissing.pastelstorage.content.block.entity.BottomlessStorageBlockEntity
import gay.menkissing.pastelstorage.content.block.entity.BottomlessStorageBlockEntity.HasFluidFilterable
import gay.menkissing.pastelstorage.content.item.BottomlessBottleItem
import gay.menkissing.pastelstorage.registries.PastelStorageComponents
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.fluids.{FluidStack, SimpleFluidContent}
import net.neoforged.neoforge.fluids.capability.IFluidHandler

final class BottleFluidStorageHandler(val parent: BottomlessStorageBlockEntity, val stack: ItemStack) extends IFluidHandler, HasFluidFilterable {

  import BottomlessStorageBlockEntity.StorageTests

  def isValidStack(stack: ItemStack): Boolean =
    StorageTests.isBottle(stack)

  def stackMax(stack: ItemStack): Int =
    if isValidStack(stack) then
      BottomlessBottleItem.getMaxStack(parent.getLevel, stack)
    else
      0

  var filter: FluidResource = FluidResource.EMPTY

  override def getFilter: FluidResource = filter

  def unset(): Unit =
    filter = FluidResource.EMPTY

  def setFilter(filter: FluidResource): Unit =
    this.filter = filter

  def setFilterFromStack(stack: ItemStack): Unit =
    val component = stack
      .getOrDefault(PastelStorageComponents.BottomlessBottleContentsComponent, SimpleFluidContent.EMPTY)
    val fluidStack = component.copy()
    this.filter = FluidResource.of(component.getFluid, fluidStack.getComponentsPatch)

  def permits(stored: FluidResource, resource: FluidResource): Boolean =
    if !stored.isBlank && !filter.isBlank && filter != stored then
      PastelStorage.Logger.debug("Shouldn't be possible to see this due to filter jank")

    if !stored.isBlank then
      filter = stored
      parent.setChanged()

    filter.isBlank || filter == resource

  override def getTanks: Int = 1

  override def getFluidInTank(slot: Int): FluidStack =
    val bottle = stack
    if isValidStack(bottle) then
      val component = bottle
        .getOrDefault(PastelStorageComponents.BottomlessBottleContentsComponent, SimpleFluidContent.EMPTY)
      component.copy()
    else
      FluidStack.EMPTY

  override def getTankCapacity(slot: Int): Int =
    val bottle = stack
    if isValidStack(bottle) then
      stackMax(bottle)
    else
      0

  override def isFluidValid(slot: Int, fluidStack: FluidStack): Boolean = true

  override def fill(fluidStack: FluidStack, fluidAction: IFluidHandler.FluidAction): Int =
    val bottle = stack
    if isValidStack(bottle) then
      val builder = BottomlessBottleItem.SimpleFluidContentBuilder.fromStack(bottle)
      if !permits(builder.template, FluidResource.ofStack(fluidStack)) then
        return 0
      val count = builder.insert(FluidResource.ofStack(fluidStack), fluidStack.getAmount)

      if fluidAction.execute() then
        builder.buildAndSet(bottle)
        parent.setChanged()

      count
    else
      0

  override def drain(fluidStack: FluidStack, fluidAction: IFluidHandler.FluidAction): FluidStack =
    val bottle = stack
    if isValidStack(bottle) then
      val builder = BottomlessBottleItem.SimpleFluidContentBuilder.fromStack(bottle)
      val count = builder.extract(FluidResource.ofStack(fluidStack), fluidStack.getAmount)

      if fluidAction.execute() then
        builder.buildAndSet(bottle)
        parent.setChanged()

      fluidStack.copyWithAmount(count)
    else
      FluidStack.EMPTY

  override def drain(maxDrain: Int, fluidAction: IFluidHandler.FluidAction): FluidStack =
    val bottle = stack
    if isValidStack(bottle) then
      val builder = BottomlessBottleItem.SimpleFluidContentBuilder.fromStack(bottle)
      val resource = builder.template
      val count = builder.extract(resource, maxDrain)

      if fluidAction.execute() then
        builder.buildAndSet(bottle)
        parent.setChanged()

      resource.makeStack(count)
    else
      FluidStack.EMPTY

}