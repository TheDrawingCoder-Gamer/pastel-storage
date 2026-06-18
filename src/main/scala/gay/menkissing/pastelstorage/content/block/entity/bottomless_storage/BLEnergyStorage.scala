package gay.menkissing.pastelstorage.content.block.entity.bottomless_storage

import com.google.common.primitives.Ints
import gay.menkissing.pastelstorage.content.block.entity.BottomlessStorageBlockEntity
import gay.menkissing.pastelstorage.content.item.BottomlessBatteryItem
import net.neoforged.neoforge.energy.{EmptyEnergyStorage, IEnergyStorage}

class BLEnergyStorage(val parent: BottomlessStorageBlockEntity) extends IEnergyStorage:
  val storages: Array[IEnergyStorage] = Array.fill(parent.capacity)(EmptyEnergyStorage.INSTANCE)

  def removeSlot(slot: Int): Unit =
    storages(slot) = EmptyEnergyStorage.INSTANCE

  def loadBattery(slot: Int): Unit =
    val stack = parent.getItem(slot)
    storages(slot) = BottomlessBatteryItem.getStorage(stack)

  override def receiveEnergy(toReceive: Int, simulate: Boolean): Int =
    var i = 0
    var remReceive = toReceive
    var received = 0

    while i < parent.capacity do
      val storage = storages(i)
      val amount = storage.receiveEnergy(remReceive, simulate)
      remReceive -= amount
      received += amount
      if remReceive == 0 then
        if !simulate then
          parent.setChanged()
        return received

      i += 1

    if received != 0 && !simulate then
      parent.setChanged()

    received


  override def extractEnergy(toExtract: Int, simulated: Boolean): Int =
    var i = 0
    var remExtract = toExtract
    var extracted = 0
    while i < parent.capacity do
      val storage = storages(i)
      val amount = storage.extractEnergy(remExtract, simulated)
      remExtract -= amount
      extracted += amount
      if remExtract == 0 then
        if !simulated then
          parent.setChanged()
        return extracted

      i += 1

    if extracted != 0 && !simulated then
      parent.setChanged()
    extracted

  override def getEnergyStored: Int =
    Ints.saturatedCast(energyStoredLong)

  def energyStoredLong: Long =
    storages.foldLeft(0L)((x, s) => x + s.getEnergyStored.toLong)

  override def getMaxEnergyStored: Int =
    Ints.saturatedCast(capacityLong)

  def capacityLong: Long =
    storages.foldLeft(0L)((x, s) => x + s.getMaxEnergyStored.toLong)

  override def canExtract: Boolean = true

  override def canReceive: Boolean = true
