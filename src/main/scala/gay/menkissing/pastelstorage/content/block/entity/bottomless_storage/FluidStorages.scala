package gay.menkissing.pastelstorage.content.block.entity.bottomless_storage

import gay.menkissing.pastelstorage.api.fluid.FluidResource
import gay.menkissing.pastelstorage.content.block.entity.BottomlessStorageBlockEntity
import gay.menkissing.pastelstorage.content.block.entity.BottomlessStorageBlockEntity.HasFluidFilterable
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler

class FluidStorages(val parent: BottomlessStorageBlockEntity) extends IFluidHandler {
  val storages: Array[IFluidHandler & HasFluidFilterable] = Array.fill(parent.capacity)(BottomlessStorageBlockEntity.EmptyFluidSlot)

  def removeSlot(slot: Int): Unit =
    storages(slot) = BottomlessStorageBlockEntity.EmptyFluidSlot

  def loadBottleSlot(slot: Int): Unit =
    val bottle = parent.getItem(slot)
    val storage = new BottleFluidStorageHandler(parent, bottle)
    storage.setFilterFromStack(bottle)
    storages(slot) = storage

  def setVoidingSlot(slot: Int): Unit =
    storages(slot) = new VoidingFluidHandler(parent, parent.getItem(slot))

  override def getTanks: Int = parent.capacity

  override def getFluidInTank(slot: Int): FluidStack =
    storages(slot).getFluidInTank(0)

  override def getTankCapacity(slot: Int): Int =
    storages(slot).getTankCapacity(0)

  override def isFluidValid(slot: Int, fluidStack: FluidStack): Boolean =
    storages(slot).isFluidValid(0, fluidStack)

  override def fill(resource: FluidStack, fluidAction: IFluidHandler.FluidAction): Int =
    var i = 0
    var toFill = resource.getAmount
    var amountFill = 0
    while i < parent.capacity do
      val storage = storages(i)
      val amount = storage.fill(resource.copyWithAmount(toFill), fluidAction)
      toFill -= amount
      amountFill += amount
      if toFill == 0 then
        return amountFill

      i += 1

    amountFill

  override def drain(resource: FluidStack, fluidAction: IFluidHandler.FluidAction): FluidStack =
    var i = 0
    var toDrain = resource.getAmount
    var amountDrained = 0
    while i < parent.capacity do
      val storage = storages(i)
      val result = storage.drain(resource.copyWithAmount(toDrain), fluidAction)
      toDrain -= result.getAmount
      amountDrained += result.getAmount
      if toDrain == 0 then
        return resource.copyWithAmount(amountDrained)

      i += 1
    resource.copyWithAmount(amountDrained)

  override def drain(maxDrain: Int, fluidAction: IFluidHandler.FluidAction): FluidStack =
    var i = 0
    var template = FluidResource.EMPTY
    var toDrain = maxDrain
    var amountDrained = 0
    while i < parent.capacity do
      val storage = storages(i)
      val result =
        if template.isBlank then
          val r = storage.drain(toDrain, fluidAction)
          if !r.isEmpty then
            template = FluidResource.ofStack(r)
          r
        else
          storage.drain(template.makeStack(toDrain), fluidAction)

      toDrain -= result.getAmount
      amountDrained += result.getAmount

      if toDrain == 0 then
        return template.makeStack(amountDrained)

      i += 1
    if template.isBlank then
      FluidStack.EMPTY
    else
      template.makeStack(amountDrained)


}