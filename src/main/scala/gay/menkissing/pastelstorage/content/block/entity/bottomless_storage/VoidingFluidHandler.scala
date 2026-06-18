package gay.menkissing.pastelstorage.content.block.entity.bottomless_storage

import gay.menkissing.pastelstorage.api.fluid.FluidResource
import gay.menkissing.pastelstorage.content.block.entity.BottomlessStorageBlockEntity
import gay.menkissing.pastelstorage.content.block.entity.BottomlessStorageBlockEntity.HasFluidFilterable
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler

final class VoidingFluidHandler(val parent: BottomlessStorageBlockEntity, val voider: ItemStack) extends IFluidHandler, HasFluidFilterable:
  override def getTanks: Int = 1

  override def getFluidInTank(i: Int): FluidStack = FluidStack.EMPTY

  override def getTankCapacity(i: Int): Int = 0

  override def isFluidValid(i: Int, fluidStack: FluidStack): Boolean = true

  override def fill(fluidStack: FluidStack, fluidAction: IFluidHandler.FluidAction): Int =
    if !fluidStack.isEmpty && fluidAction.execute() then
      parent.tryTriggerVoidObjective(voider)
    fluidStack.getAmount

  override def drain(fluidStack: FluidStack, fluidAction: IFluidHandler.FluidAction): FluidStack =
    FluidStack.EMPTY

  override def drain(i: Int, fluidAction: IFluidHandler.FluidAction): FluidStack =
    FluidStack.EMPTY

  override def setFilter(filter: FluidResource): Unit = ()

  override def getFilter: FluidResource = FluidResource.EMPTY

  override def unset(): Unit = ()