package gay.menkissing.pastelstorage.content.block.entity

import com.google.common.primitives.Ints
import earth.terrarium.pastel.blocks.bottomless_bundle.BottomlessBundleItem
import earth.terrarium.pastel.api.item.{ItemReference, ItemStorage}
import earth.terrarium.pastel.helpers.Support
import earth.terrarium.pastel.registries.{PastelBlocks, PastelDataComponentTypes, PastelEnchantmentTags, PastelEnchantments}
import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.content.PastelStorageBlocks.bottomlessBarrel
import gay.menkissing.pastelstorage.content.block.BottomlessShelfBlock
import gay.menkissing.pastelstorage.content.item.{BottomlessBatteryItem, BottomlessBottleItem}
import gay.menkissing.pastelstorage.content.{PastelStorageBlocks, PastelStorageItems}
import gay.menkissing.pastelstorage.registries.{PastelStorageComponents, PastelStorageCriteria, PastelStorageTags, PastelStorageTranslationKeys}
import gay.menkissing.pastelstorage.screen.BottomlessStorageMenu
import gay.menkissing.pastelstorage.api.fluid.FluidResource
import gay.menkissing.pastelstorage.content.block.entity.BottomlessStorageBlockEntity.{HasFluidFilterable, HasItemFilterable}
import gay.menkissing.pastelstorage.util.PastelStorageEnchantmentHelper
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.{BuiltInRegistries, Registries}
import net.minecraft.core.{BlockPos, Direction, HolderLookup, NonNullList, Vec3i}
import net.minecraft.nbt.{CompoundTag, ListTag, NbtOps, Tag}
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Component.Serializer
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.{SoundEvent, SoundEvents, SoundSource}
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.{AbstractContainerMenu, ChestMenu}
import net.minecraft.world.{Clearable, Container, ContainerHelper, Containers, MenuProvider, Nameable}
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.{EnchantmentHelper, Enchantments}
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BarrelBlock
import net.minecraft.world.level.block.entity.{BaseContainerBlockEntity, BlockEntity, BlockEntityType, ContainerOpenersCounter}
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.capabilities.{Capabilities, RegisterCapabilitiesEvent}
import net.neoforged.neoforge.energy.{EmptyEnergyStorage, IEnergyStorage}
import net.neoforged.neoforge.fluids.{FluidStack, SimpleFluidContent}
import net.neoforged.neoforge.fluids.capability.IFluidHandler
import net.neoforged.neoforge.items.IItemHandler

import java.util.{Objects, Optional}
import scala.jdk.CollectionConverters.*

abstract class BottomlessStorageBlockEntity(val capacity: Int, baseEntity: BlockEntityType[? <: BottomlessStorageBlockEntity], pos: BlockPos, state: BlockState) extends BlockEntity(baseEntity, pos, state), Clearable:


  protected val items: NonNullList[ItemStack] = NonNullList.withSize(capacity, ItemStack.EMPTY)
  protected var lastInteractedSlot: Int = -1


  val itemStorage: ItemStorages = new ItemStorages
  val fluidStorage: FluidStorages = new FluidStorages
  val energyStorage: EnergyStorages = new EnergyStorages

  def tryTriggerVoidObjective(stack: ItemStack): Unit =
    if !stack.isEmpty then
      level match
        case sl: ServerLevel =>
          Support
            .areaCriterion(sl, 5, getBlockPos, ResourceLocation.fromNamespaceAndPath("pastel", "midgame/pastel_midgame"), p => PastelStorageCriteria.BARREL_VOIDING
                                                                                                         .trigger(p, stack))
        case _ => ()

  object StorageManager:
    def removeSlot(slot: Int): Unit =
      itemStorage.removeSlot(slot)
      fluidStorage.removeSlot(slot)
      energyStorage.removeSlot(slot)

    def loadBundleSlot(slot: Int): Unit =
      fluidStorage.removeSlot(slot)
      energyStorage.removeSlot(slot)
      itemStorage.loadBundleSlot(slot)

    def loadBottleSlot(slot: Int): Unit =
      itemStorage.removeSlot(slot)
      energyStorage.removeSlot(slot)
      fluidStorage.loadBottleSlot(slot)

    def loadBatterySlot(slot: Int): Unit =
      itemStorage.removeSlot(slot)
      fluidStorage.removeSlot(slot)
      energyStorage.loadBattery(slot)


    def setVoidingSlot(slot: Int): Unit =
      fluidStorage.setVoidingSlot(slot)
      itemStorage.setVoidingSlot(slot)
      // No voiding for energy, at least not in the same way as item/fluid
      energyStorage.removeSlot(slot)


  // TODO: Optimize these storages so they don't need to constantly recheck the stack

  final class ItemStorages extends IItemHandler {
    val storages: Array[IItemHandler & HasItemFilterable] = Array.fill(capacity)(BottomlessStorageBlockEntity.EmptyItemSlot)

    def unsetSlot(slot: Int): Unit =
      storages(slot).unset()

    def removeSlot(slot: Int): Unit =
      storages(slot) = BottomlessStorageBlockEntity.EmptyItemSlot

    def loadBundleSlot(slot: Int): Unit =
      val stack = items.get(slot)
      val storage = new BundleItemStorageHandler(stack)
      val filter = stack.getOrDefault(PastelDataComponentTypes.ITEM_STORAGE, ItemStorage.Component.DEFAULT)
      storage.setFilter(filter.reference())
      storages(slot) = storage

    def setVoidingSlot(slot: Int): Unit =
      storages(slot) = VoidingItemSlot(items.get(slot))

    def setSlotFilter(slot: Int, stack: ItemReference): Unit =
      storages(slot).setFilter(stack)

    override def getSlots: Int = capacity

    override def getStackInSlot(slot: Int): ItemStack =
      storages(slot).getStackInSlot(0)

    override def getSlotLimit(slot: Int): Int =
      storages(slot).getSlotLimit(0)

    override def insertItem(slot: Int, stack: ItemStack, simulated: Boolean): ItemStack =
      storages(slot).insertItem(0, stack, simulated)

    override def extractItem(slot: Int, amount: Int, simulated: Boolean): ItemStack =
      storages(slot).extractItem(0, amount, simulated)

    override def isItemValid(slot: Int, stack: ItemStack): Boolean = true
  }

  final class VoidingItemSlot(val voider: ItemStack) extends IItemHandler, HasItemFilterable {

    override def getSlots: Int = 1

    override def getStackInSlot(i: Int): ItemStack = ItemStack.EMPTY

    override def insertItem(i: Int, itemStack: ItemStack, simulate: Boolean): ItemStack =
      if !itemStack.isEmpty && !simulate then
        tryTriggerVoidObjective(voider)
      ItemStack.EMPTY

    override def extractItem(i: Int, i1: Int, b: Boolean): ItemStack =
      ItemStack.EMPTY

    override def getSlotLimit(i: Int): Int = 0

    override def isItemValid(i: Int, itemStack: ItemStack): Boolean = true

    override def setFilter(filter: ItemReference): Unit = ()

    override def getFilter: ItemReference = ItemReference.empty()

    override def unset(): Unit = ()
  }

  final class BundleItemStorageHandler(val stack: ItemStack) extends IItemHandler, HasItemFilterable {
    import BottomlessStorageBlockEntity.StorageTests
    // def stack: ItemStack = items.get(slot)

    def isVoidingStack(stack: ItemStack): Boolean =
      EnchantmentHelper.hasTag(stack, PastelEnchantmentTags.DELETES_OVERFLOW)

    def isValidStack(stack: ItemStack): Boolean =
      StorageTests.isBundle(stack)

    def stackMax(stack: ItemStack): Int =
      if isValidStack(stack) then
        BottomlessBundleItem.getMaxStoredAmount(
          PastelStorageEnchantmentHelper.getLevel(level.registryAccess(), Enchantments.POWER, stack)
        ).toInt
      else
        0


    var filter: ItemReference = ItemReference.empty()


    def unset(): Unit =
      this.filter = ItemReference.empty()

    def setFilter(filter: ItemReference): Unit =
      this.filter = filter

    def getFilter: ItemReference = filter


    def permits(stored: ItemReference, resource: ItemReference): Boolean =
      // assume(!this.isGarbageStack(this.stack))
      if !stored.isEmpty && !filter.isEmpty && filter != stored then
        PastelStorage.Logger.info("player must have modified the bundle manually, updating filter")

      if !stored.isEmpty then
        filter = stored
        setChanged()

      filter.isEmpty || filter == resource

    override def getSlots: Int = 1

    override def getStackInSlot(i: Int): ItemStack =
      val bundle = this.stack
      if isValidStack(bundle) then
        val storage = ItemStorage.load(bundle)
        storage.stack(math.min(storage.getCount, storage.stackSize()).toInt).copy()
      else
        ItemStack.EMPTY

    override def insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack =
      val bundle = this.stack
      if isValidStack(bundle) && slot == 0 then
        val storage = ItemStorage.load(bundle)
        if !permits(storage.getReference, ItemReference.of(stack)) then
          return stack
        val change = storage.insert(stack)
        val remainder = stack.copyWithCount(stack.getCount - change)

        if simulate then
          remainder
        else
          storage.copy().save(bundle)
          setChanged()
          if isVoidingStack(bundle) then
            ItemStack.EMPTY
          else
            remainder
      else
        stack

    override def extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack =
      val bundle = this.stack
      // no special casing for garbage here
      if isValidStack(stack) && slot == 0 then
        val storage = ItemStorage.load(bundle)
        val result = storage.extract(amount)

        if !simulate then
          setChanged()
          storage.copy().save(bundle)
        result
      else
        ItemStack.EMPTY

    override def getSlotLimit(slot: Int): Int =
      val bundle = this.stack
      if isValidStack(bundle) then
        stackMax(bundle)
      else
        0

    override def isItemValid(slot: Int, itemStack: ItemStack): Boolean =
      true

  }

  class FluidStorages extends IFluidHandler {
    val storages: Array[IFluidHandler & HasFluidFilterable] = Array.fill(capacity)(BottomlessStorageBlockEntity.EmptyFluidSlot)

    def removeSlot(slot: Int): Unit =
      storages(slot) = BottomlessStorageBlockEntity.EmptyFluidSlot

    def loadBottleSlot(slot: Int): Unit =
      val bottle = items.get(slot)
      val storage = new BottleFluidStorageHandler(bottle)
      storage.setFilterFromStack(bottle)
      storages(slot) = storage

    def setVoidingSlot(slot: Int): Unit =
      storages(slot) = new VoidingFluidHandler(items.get(slot))

    override def getTanks: Int = capacity

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
      while i < capacity do
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
      while i < capacity do
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
      while i < capacity do
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

  final class VoidingFluidHandler(val voider: ItemStack) extends IFluidHandler, HasFluidFilterable:
    override def getTanks: Int = 1

    override def getFluidInTank(i: Int): FluidStack = FluidStack.EMPTY

    override def getTankCapacity(i: Int): Int = 0

    override def isFluidValid(i: Int, fluidStack: FluidStack): Boolean = true

    override def fill(fluidStack: FluidStack, fluidAction: IFluidHandler.FluidAction): Int =
      if !fluidStack.isEmpty && fluidAction.execute() then
        tryTriggerVoidObjective(voider)
      fluidStack.getAmount

    override def drain(fluidStack: FluidStack, fluidAction: IFluidHandler.FluidAction): FluidStack =
      FluidStack.EMPTY

    override def drain(i: Int, fluidAction: IFluidHandler.FluidAction): FluidStack =
      FluidStack.EMPTY

    override def setFilter(filter: FluidResource): Unit = ()

    override def getFilter: FluidResource = FluidResource.EMPTY

    override def unset(): Unit = ()

  final class BottleFluidStorageHandler(val stack: ItemStack) extends IFluidHandler, HasFluidFilterable {
    import BottomlessStorageBlockEntity.StorageTests

    def isValidStack(stack: ItemStack): Boolean =
      StorageTests.isBottle(stack)

    def stackMax(stack: ItemStack): Int =
      if isValidStack(stack) then
        BottomlessBottleItem.getMaxStack(level, stack)
      else
        0

    var filter: FluidResource = FluidResource.EMPTY

    override def getFilter: FluidResource = filter

    def unset(): Unit =
      filter = FluidResource.EMPTY

    def setFilter(filter: FluidResource): Unit =
      this.filter = filter

    def setFilterFromStack(stack: ItemStack): Unit =
      val component = stack.getOrDefault(PastelStorageComponents.BottomlessBottleContentsComponent, SimpleFluidContent.EMPTY)
      val fluidStack = component.copy()
      this.filter = FluidResource.of(component.getFluid, fluidStack.getComponentsPatch)
    def permits(stored: FluidResource, resource: FluidResource): Boolean =
      if !stored.isBlank && !filter.isBlank && filter != stored then
        PastelStorage.Logger.debug("Shouldn't be possible to see this due to filter jank")

      if !stored.isBlank then
        filter = stored
        setChanged()

      filter.isBlank || filter == resource

    override def getTanks: Int = 1

    override def getFluidInTank(slot: Int): FluidStack =
      val bottle = stack
      if isValidStack(bottle) then
        val component = bottle.getOrDefault(PastelStorageComponents.BottomlessBottleContentsComponent, SimpleFluidContent.EMPTY)
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
          setChanged()

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
          setChanged()

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
          setChanged()

        resource.makeStack(count)
      else
        FluidStack.EMPTY

  }

  final class EnergyStorages extends IEnergyStorage:
    val storages: Array[IEnergyStorage] = Array.fill(capacity)(EmptyEnergyStorage.INSTANCE)

    def removeSlot(slot: Int): Unit =
      storages(slot) = EmptyEnergyStorage.INSTANCE

    def loadBattery(slot: Int): Unit =
      val stack = items.get(slot)
      storages(slot) = BottomlessBatteryItem.getStorage(stack)

    override def receiveEnergy(toReceive: Int, simulate: Boolean): Int =
      var i = 0
      var remReceive = toReceive
      var received = 0

      while i < capacity do
        val storage = storages(i)
        val amount = storage.receiveEnergy(remReceive, simulate)
        remReceive -= amount
        received += amount
        if remReceive == 0 then
          if !simulate then
            setChanged()
          return received

        i += 1

      if received != 0 && !simulate then
        setChanged()

      received



    override def extractEnergy(toExtract: Int, simulated: Boolean): Int =
      var i = 0
      var remExtract = toExtract
      var extracted = 0
      while i < capacity do
        val storage = storages(i)
        val amount = storage.extractEnergy(remExtract, simulated)
        remExtract -= amount
        extracted += amount
        if remExtract == 0 then
          if !simulated then
            setChanged()
          return extracted

        i += 1

      if extracted != 0 && !simulated then
        setChanged()
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


  override protected def loadAdditional(tag: CompoundTag, lookup: HolderLookup.Provider): Unit =
    super.loadAdditional(tag, lookup)
    ContainerHelper.loadAllItems(tag, items, lookup)
    // need to update it here so the correct kind is selected _before_ we start applying filters
    (0 until capacity).foreach(updateSlot)
    this.loadItemFilters(tag, lookup)
    this.loadFluidFilters(tag, lookup)
    this.lastInteractedSlot = tag.getInt("last_interacted_slot")

  override protected def saveAdditional(tag: CompoundTag, lookup: HolderLookup.Provider): Unit =
    super.saveAdditional(tag, lookup)
    ContainerHelper.saveAllItems(tag, items, lookup)
    this.saveItemFilters(tag, lookup)
    this.saveFluidFilters(tag, lookup)
    tag.putInt("last_interacted_slot", lastInteractedSlot)

  def clearContent(): Unit =
    (0 until capacity).foreach(StorageManager.removeSlot)
    items.clear()

  def isEmpty: Boolean =
    items.stream().allMatch(_.isEmpty)

  protected def updateSlot(slot: Int): Unit =
    val stack = this.items.get(slot)
    if stack.is(PastelStorageItems.bottomlessBottle) then
      StorageManager.loadBottleSlot(slot)
    else if stack.is(PastelBlocks.BOTTOMLESS_BUNDLE.asItem()) then
      StorageManager.loadBundleSlot(slot)
    else if stack.is(PastelStorageItems.bottomlessBattery) then
      StorageManager.loadBatterySlot(slot)
    else if stack.is(PastelStorageTags.item.deletesItemsWhenInsertedInto) then
      StorageManager.setVoidingSlot(slot)
    else
      StorageManager.removeSlot(slot)

  protected def updateSlotShown(slot: Int): Unit = ()

  def loadItemFilters(tag: CompoundTag, provider: HolderLookup.Provider): Unit =
    val listTag = tag.getList(BottomlessStorageBlockEntity.tagItemFilters, 10)
    val context = provider.createSerializationContext(NbtOps.INSTANCE)
    for i <- 0 until listTag.size() do
      val compound = listTag.getCompound(i)
      val j = compound.getByte("Slot").toInt
      if j >= 0 && j < capacity then
        // if error then will auto return blankie wankie
        val variant = ItemReference.CODEC.decode(context, compound).getOrThrow()
        itemStorage.storages(j).setFilter(variant.getFirst)

  def loadFluidFilters(tag: CompoundTag, provider: HolderLookup.Provider): Unit =
    val listTag = tag.getList(BottomlessStorageBlockEntity.tagFluidFilters, 10)
    val context = provider.createSerializationContext(NbtOps.INSTANCE)
    for i <- 0 until listTag.size() do
      val compound = listTag.getCompound(i)
      val j = compound.getByte("Slot").toInt
      if j >= 0 && j < capacity then
        val variant = FluidResource.CODEC.decode(context, compound).getOrThrow().getFirst
        fluidStorage.storages(j).setFilter(variant)

  def saveItemFilters(tag: CompoundTag, provider: HolderLookup.Provider): Unit =
    val listTag = ListTag()
    val context = provider.createSerializationContext(NbtOps.INSTANCE)
    for i <- 0 until capacity do
      val filter = itemStorage.storages(i).getFilter
      if !filter.isEmpty then
        val compound = CompoundTag()
        compound.putByte("Slot", i.toByte)
        val compound2 = ItemReference.CODEC.encode(filter, context, compound).getOrThrow()
        listTag.add(compound2)
    tag.put(BottomlessStorageBlockEntity.tagItemFilters, listTag)

  def saveFluidFilters(tag: CompoundTag, provider: HolderLookup.Provider): Unit =
    val listTag = ListTag()
    val context = provider.createSerializationContext(NbtOps.INSTANCE)
    for i <- 0 until 6 do
      val filter = fluidStorage.storages(i).getFilter
      if !filter.isBlank then
        val compound = CompoundTag()
        compound.putByte("Slot", i.toByte)
        val compound2 = FluidResource.CODEC.encode(filter, context, compound).getOrThrow()
        listTag.add(compound2)
    tag.put(BottomlessStorageBlockEntity.tagFluidFilters, listTag)

  def getItem(slot: Int): ItemStack = this.items.get(slot)

  def removeItem(slot: Int, amount: Int): ItemStack =
    val stack = Objects.requireNonNullElse(this.items.get(slot), ItemStack.EMPTY)
    this.items.set(slot, ItemStack.EMPTY)
    updateSlot(slot)
    if !stack.isEmpty then
      this.updateSlotShown(slot)

    stack

  def removeItemNoUpdate(slot: Int): ItemStack = this.removeItem(slot, 1)

  def setItem(slot: Int, stack: ItemStack): Unit =
    if BottomlessStorageBlockEntity.StorageTests.isAccepted(stack) then
      this.items.set(slot, stack)
      this.updateSlot(slot)
      this.updateSlotShown(slot)
    else if stack.isEmpty then
      this.removeItem(slot, 1)

  def stillValid(player: Player): Boolean = Container.stillValidBlockEntity(this, player)

  def getMaxStackSize(stack: ItemStack): Int = 1





abstract class ContainerBottomlessStorageBlockEntity(capacity: Int, baseEntity: BlockEntityType[? <: BottomlessStorageBlockEntity], pos: BlockPos, state: BlockState) extends BottomlessStorageBlockEntity(capacity, baseEntity, pos, state), MenuProvider, NameableBlockEntity:
  protected val openSound: SoundEvent = SoundEvents.BARREL_OPEN
  protected val closeSound: SoundEvent = SoundEvents.BARREL_CLOSE


  val containerView = new ContainerForBottomlessStorage()


  private val openersCounter: ContainerOpenersCounter =
    new ContainerOpenersCounter:
      override def onOpen(level: Level, blockPos: BlockPos, blockState: BlockState): Unit =
        ContainerBottomlessStorageBlockEntity.this.playSound(blockState, SoundEvents.BARREL_OPEN)
        ContainerBottomlessStorageBlockEntity.this.updateBlockState(blockState, true)

      override def onClose(level: Level, blockPos: BlockPos, blockState: BlockState): Unit =
        ContainerBottomlessStorageBlockEntity.this.playSound(blockState, SoundEvents.BARREL_CLOSE)
        ContainerBottomlessStorageBlockEntity.this.updateBlockState(blockState, false)

      override def openerCountChanged(level: Level, blockPos: BlockPos, blockState: BlockState, i: Int, j: Int): Unit = ()

      override def isOwnContainer(player: Player): Boolean =
        player.containerMenu match
          case ce: BottomlessStorageMenu =>
            ce.container match
              case cfbs: ContainerForBottomlessStorage =>
                cfbs.parent == ContainerBottomlessStorageBlockEntity.this
              case _ => false
          case _ => false

  private def incrementOpeners(player: Player): Unit =
    this.openersCounter.incrementOpeners(
      player,
      this.getLevel,
      this.getBlockPos,
      this.getBlockState
    )

  private def decrementOpeners(player: Player): Unit =
    this.openersCounter.decrementOpeners(
      player,
      this.getLevel,
      this.getBlockPos,
      this.getBlockState
    )

  def defaultName: Component

  final class ContainerForBottomlessStorage extends Container:
    def parent: ContainerBottomlessStorageBlockEntity = ContainerBottomlessStorageBlockEntity.this

    def getContainerSize: Int = capacity


    override def startOpen(player: Player): Unit =
      if !ContainerBottomlessStorageBlockEntity.this.remove && !player.isSpectator then
        incrementOpeners(player)

    override def stopOpen(player: Player): Unit =
      if !ContainerBottomlessStorageBlockEntity.this.remove && !player.isSpectator then
        decrementOpeners(player)

    override def clearContent(): Unit =
      items.clear()
      (0 until capacity).foreach(StorageManager.removeSlot)

    override def getItem(i: Int): ItemStack =
      items.get(i)

    override def isEmpty: Boolean = ContainerBottomlessStorageBlockEntity.this.isEmpty

    override def removeItem(slot: Int, amount: Int): ItemStack =
      ContainerBottomlessStorageBlockEntity.this.removeItem(slot, amount)

    override def removeItemNoUpdate(slot: Int): ItemStack =
      ContainerBottomlessStorageBlockEntity.this.removeItemNoUpdate(slot)

    override def setChanged(): Unit =
      // Better safe than soggy.......
      ContainerBottomlessStorageBlockEntity.this.setChanged()

    override def setItem(slot: Int, stack: ItemStack): Unit =
      ContainerBottomlessStorageBlockEntity.this.setItem(slot, stack)

    override def stillValid(player: Player): Boolean =
      ContainerBottomlessStorageBlockEntity.this.stillValid(player)

  def recheckOpen(): Unit =
    if !this.remove then
      openersCounter.recheckOpeners(this.getLevel, this.getBlockPos, this.getBlockState)




  def playSound(blockState: BlockState, soundEvent: SoundEvent): Unit =
    val vec3i = blockState.getValue(BarrelBlock.FACING).getNormal
    val d = this.worldPosition.getX.toDouble + 0.5F.toDouble + (vec3i.getX.toDouble / 2.0F.toDouble)
    val e = this.worldPosition.getY.toDouble + 0.5F.toDouble + (vec3i.getY.toDouble / 2.0F.toDouble)
    val f = this.worldPosition.getZ.toDouble + 0.5F.toDouble + (vec3i.getZ.toDouble / 2.0F.toDouble)
    this.level.playSound(
      null,
      d, e, f,
      soundEvent,
      SoundSource.BLOCKS,
      0.5F,
      this.level.random.nextFloat * 0.1F + 0.9F
    )

  def updateBlockState(state: BlockState, open: Boolean): Unit =
    this.level.setBlockAndUpdate(this.getBlockPos, state.setValue(BarrelBlock.OPEN, open))

  override def getDisplayName: Component = getName


  def getContainerSize: Int = capacity

object BottomlessStorageBlockEntity:
  val tagFluidFilters = "fluid_filters"
  val tagItemFilters = "item_filters"
  final class BottomlessBarrelBlockEntity(pos: BlockPos, state: BlockState) extends ContainerBottomlessStorageBlockEntity(BottomlessStorageMenu.barrelContainerSize, PastelStorageBlocks.bottomlessBarrelBlockEntity.get(), pos, state):
    override def defaultName: Component = PastelStorageTranslationKeys.container.bottomlessBarrel

    override def createMenu(windowId: Int, inventory: Inventory, player: Player): AbstractContainerMenu =
      BottomlessStorageMenu.barrelServer(windowId, inventory, containerView)


  final class BottomlessAmphoraBlockEntity(pos: BlockPos, state: BlockState) extends ContainerBottomlessStorageBlockEntity(BottomlessStorageMenu.amphoraContainerSize, PastelStorageBlocks.bottomlessAmphoraBlockEntity.get(), pos, state):
    override def defaultName: Component = PastelStorageTranslationKeys.container.bottomlessAmphora

    override def createMenu(windowId: Int, inventory: Inventory, player: Player): AbstractContainerMenu =
      BottomlessStorageMenu.amphoraServer(windowId, inventory, containerView)

  trait HasItemFilterable:
    def setFilter(filter: ItemReference): Unit
    def getFilter: ItemReference
    def unset(): Unit

  trait HasFluidFilterable:
    def setFilter(filter: FluidResource): Unit
    def getFilter: FluidResource
    def unset(): Unit

  object EmptyItemSlot extends IItemHandler, HasItemFilterable {
    override def getSlots: Int = 1

    override def getStackInSlot(i: Int): ItemStack = ItemStack.EMPTY

    override def insertItem(i: Int, itemStack: ItemStack, b: Boolean): ItemStack =
      itemStack

    override def extractItem(i: Int, i1: Int, b: Boolean): ItemStack = ItemStack.EMPTY

    override def getSlotLimit(i: Int): Int = 0

    override def isItemValid(i: Int, itemStack: ItemStack): Boolean = true

    override def setFilter(filter: ItemReference): Unit = ()

    override def unset(): Unit = ()

    override def getFilter: ItemReference = ItemReference.empty()
  }

  object EmptyFluidSlot extends IFluidHandler, HasFluidFilterable:
    override def setFilter(filter: FluidResource): Unit = ()

    override def getFilter: FluidResource = FluidResource.EMPTY

    override def unset(): Unit = ()

    override def getTanks: Int = 1

    override def getFluidInTank(i: Int): FluidStack = FluidStack.EMPTY

    override def getTankCapacity(i: Int): Int = 0

    override def isFluidValid(i: Int, fluidStack: FluidStack): Boolean = true

    override def fill(fluidStack: FluidStack, fluidAction: IFluidHandler.FluidAction): Int =
      0

    override def drain(fluidStack: FluidStack, fluidAction: IFluidHandler.FluidAction): FluidStack =
      FluidStack.EMPTY

    override def drain(i: Int, fluidAction: IFluidHandler.FluidAction): FluidStack =
      FluidStack.EMPTY

  object VoidingEnergySlot extends IEnergyStorage:
    override def receiveEnergy(toReceive: Int, b: Boolean): Int = toReceive

    override def extractEnergy(i: Int, b: Boolean): Int = 0

    override def getEnergyStored: Int = 0

    override def getMaxEnergyStored: Int = 0

    override def canExtract: Boolean = false

    override def canReceive: Boolean = true

  def dropContents(level: Level, pos: BlockPos, entity: BottomlessStorageBlockEntity): Unit =
    (0 until entity.capacity).foreach: slot =>
      val item = entity.getItem(slot)
      if !item.isEmpty then
        Containers.dropItemStack(level, pos.getX, pos.getY, pos.getZ, item)

    entity.clearContent()


  def removeContainer(level: Level, pos: BlockPos, player: Player, blockEntity: BottomlessStorageBlockEntity, slot: Int): Unit =
    if !level.isClientSide then
      val stack = blockEntity.removeItem(slot, 1)
      if !player.getInventory.add(stack) then
        player.drop(stack, false)

      level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos)

  object StorageTests:
    def isAccepted(stack: ItemStack): Boolean =
      isBundle(stack) || isBottle(stack) || isBattery(stack) || isGarbage(stack)

    def isBattery(stack: ItemStack): Boolean =
      stack.is(PastelStorageItems.bottomlessBattery)

    def isBundle(stack: ItemStack): Boolean =
      stack.is(PastelBlocks.BOTTOMLESS_BUNDLE.asItem())

    def isBottle(stack: ItemStack): Boolean =
      stack.is(PastelStorageItems.bottomlessBottle)

    def isGarbage(stack: ItemStack): Boolean =
      stack.is(PastelStorageTags.item.deletesItemsWhenInsertedInto)

  def registerStorages(bus: IEventBus): Unit =
    import PastelStorageBlocks.{
      bottomlessAmphoraBlockEntity => bottomlessAmphoraBE,
      bottomlessShelfBlockEntity => bottomlessShelfBE,
      bottomlessBarrelBlockEntity => bottomlessBarrelBE
    }
    // fingers crossed this works?
    bus.addListener: (ev: RegisterCapabilitiesEvent) =>
      List[BlockEntityType[? <: BottomlessStorageBlockEntity]](bottomlessShelfBE.get(), bottomlessAmphoraBE.get(), bottomlessBarrelBE.get()).foreach: entity =>
        ev.registerBlockEntity(
          Capabilities.ItemHandler.BLOCK,
          entity,
          (en, side) => en.itemStorage
        )
        ev.registerBlockEntity(
          Capabilities.FluidHandler.BLOCK,
          entity,
          (en, side) => en.fluidStorage
        )
        ev.registerBlockEntity(
          Capabilities.EnergyStorage.BLOCK,
          entity,
          (en, side) => en.energyStorage
        )