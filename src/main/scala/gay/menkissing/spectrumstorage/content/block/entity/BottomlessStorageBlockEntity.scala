package gay.menkissing.spectrumstorage.content.block.entity

import de.dafuqs.spectrum.blocks.bottomless_bundle.BottomlessBundleItem
import de.dafuqs.spectrum.blocks.bottomless_bundle.BottomlessBundleItem.BottomlessStack
import de.dafuqs.spectrum.registries.{SpectrumBlocks, SpectrumDataComponentTypes, SpectrumEnchantmentTags, SpectrumEnchantments, SpectrumItems}
import gay.menkissing.spectrumstorage.SpectrumStorage
import gay.menkissing.spectrumstorage.content.block.BottomlessShelfBlock
import gay.menkissing.spectrumstorage.content.item.BottomlessBottleItem
import gay.menkissing.spectrumstorage.content.{SpectrumStorageBlocks, SpectrumStorageItems}
import gay.menkissing.spectrumstorage.screen.BottomlessStorageMenu
import gay.menkissing.spectrumstorage.util.LumoEnchantmentHelper
import net.fabricmc.fabric.api.transfer.v1.fluid.{FluidStorage, FluidVariant}
import net.fabricmc.fabric.api.transfer.v1.item.{ItemStorage, ItemVariant}
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.storage.base.{CombinedSlottedStorage, CombinedStorage, ResourceAmount, SingleSlotStorage}
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.{BlockPos, Direction, HolderLookup, NonNullList, Vec3i}
import net.minecraft.nbt.{CompoundTag, ListTag, NbtOps, Tag}
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Component.Serializer
import net.minecraft.sounds.{SoundEvent, SoundEvents, SoundSource}
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.{AbstractContainerMenu, ChestMenu}
import net.minecraft.world.{Container, ContainerHelper, Containers, MenuProvider, Nameable}
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.{EnchantmentHelper, Enchantments}
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BarrelBlock
import net.minecraft.world.level.block.entity.{BaseContainerBlockEntity, BlockEntity, BlockEntityType, ContainerOpenersCounter}
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent

import java.util.Objects
import scala.jdk.CollectionConverters.*

abstract class BottomlessStorageBlockEntity(val capacity: Int, baseEntity: BlockEntityType[BottomlessStorageBlockEntity], pos: BlockPos, state: BlockState) extends BlockEntity(baseEntity, pos, state):

  import BottomlessStorageBlockEntity.BundleHelper



  protected val items = NonNullList.withSize(capacity, ItemStack.EMPTY)
  protected var lastInteractedSlot: Int = -1


  val itemStorage: CombinedSlottedStorage[ItemVariant, BundleItemStorageWrapper] =
    CombinedSlottedStorage((0 until capacity).map(BundleItemStorageWrapper(_)).toList.asJava)
  val fluidStorage: CombinedSlottedStorage[FluidVariant, BottleFluidStorageWrapper] =
    CombinedSlottedStorage((0 until capacity).map(BottleFluidStorageWrapper(_)).toList.asJava)

  class BundleItemStorageWrapper(val slot: Int) extends SnapshotParticipant[ResourceAmount[ItemVariant]], SingleSlotStorage[ItemVariant]:
    var filter: ItemVariant = ItemVariant.blank()

    def resetVariant(): Unit = filter = ItemVariant.blank()

    def validVariant(storedVariant: ItemVariant, resource: ItemVariant): Boolean =
      if !storedVariant.isBlank && !filter.isBlank && filter != storedVariant then
        SpectrumStorage.Logger.debug("Player must have modified the bundle manually, updating filter")

      if !storedVariant.isBlank then
        filter = storedVariant



      filter.isBlank || filter == resource

    def bundle: Option[ItemStack] =
      val thingie = BottomlessStorageBlockEntity.this.items.get(slot)
      Option.when(thingie.is(SpectrumBlocks.BOTTOMLESS_BUNDLE.asItem()))(thingie)

    override def insert(resource: ItemVariant, maxAmount: Long, transaction: TransactionContext): Long =
      StoragePreconditions.notBlankNotNegative(resource, maxAmount)

      this.bundle match
        case None => 0L
        case Some(bundle) =>
          val builder = BundleHelper.buildFromStack(bundle)


          if validVariant(builder.template, resource) then
            this.updateSnapshots(transaction)
            val inserted = builder.insert(resource, maxAmount)
            builder.buildAndSet(bundle)

            inserted
          else
            0L

    override def extract(resource: ItemVariant, maxAmount: Long, transaction: TransactionContext): Long =
      StoragePreconditions.notBlankNotNegative(resource, maxAmount)

      this.bundle match
        case None => 0L
        case Some(bundle) =>
          val builder = BundleHelper.buildFromStack(bundle)

          if validVariant(builder.template, resource) then
            this.updateSnapshots(transaction)
            val extracted = builder.extract(resource, maxAmount)
            builder.buildAndSet(bundle)

            extracted
          else
            0L

    override def isResourceBlank: Boolean =
      this.bundle.forall: stack =>
        val contents = BundleHelper.contentsFromStack(stack)
        contents.count() == 0L || contents.variant().isBlank

    override def getResource: ItemVariant =
      this.bundle match
        case None => ItemVariant.blank()
        case Some(bundle) => BundleHelper.contentsFromStack(bundle).variant

    override def getAmount: Long =
      this.bundle match
        case None => 0L
        case Some(bundle) => BundleHelper.contentsFromStack(bundle).count

    override def getCapacity: Long =
      this.bundle match
        case None => 0L
        case Some(bundle) => BottomlessBundleItem
          .getMaxStoredAmount(LumoEnchantmentHelper.getLevel(level.registryAccess(), Enchantments.POWER, bundle))

    override def createSnapshot(): ResourceAmount[ItemVariant] =
      this.bundle match
        case None => ResourceAmount(ItemVariant.blank(), 0L)
        case Some(bundle) =>
          val contents = BundleHelper.contentsFromStack(bundle)
          ResourceAmount(contents.variant, contents.count)

    override def readSnapshot(snapshot: ResourceAmount[ItemVariant]): Unit =
      this.bundle match
        case None => ()
        case Some(bundle) =>
          val builder = BundleHelper.buildFromStack(bundle)
          builder.set(snapshot.resource(), snapshot.amount())
          builder.buildAndSet(bundle)

    override def onFinalCommit(): Unit =
      // better safe than soggy...
      BottomlessStorageBlockEntity.this.setChanged()

  class BottleFluidStorageWrapper(val slot: Int) extends SnapshotParticipant[ResourceAmount[FluidVariant]], SingleSlotStorage[FluidVariant]:
    var filter: FluidVariant = FluidVariant.blank()


    def resetVariant(): Unit = filter = FluidVariant.blank()

    def bottle: Option[ItemStack] =
      val thingie = BottomlessStorageBlockEntity.this.items.get(slot)
      Option.when(thingie.is(SpectrumStorageItems.bottomlessBottle))(thingie)

    def validVariant(storedVariant: FluidVariant, resource: FluidVariant): Boolean =
      if filter.isBlank && !storedVariant.isBlank then
        filter = storedVariant

      // This assertion still works for fluids because players cant manipulate the bottle without removing and reinserting it,
      // and when they do that the filter will be reinspected anyway
      if !storedVariant.isBlank then
        assert(storedVariant == filter)

      filter.isBlank || filter == resource

    override def insert(resource: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long = {
      StoragePreconditions.notBlankNotNegative(resource, maxAmount)

      this.bottle match
        case None => 0L
        case Some(bottle) =>
          val builder = BottomlessBottleItem.BottomlessBottleContents.Builder.of(BottomlessStorageBlockEntity.this.level, bottle)

          if validVariant(builder.template, resource) then
            this.updateSnapshots(transaction)
            val inserted = builder.insert(resource, maxAmount)
            BottomlessBottleItem.BottomlessBottleContents.replaceInStack(bottle, builder.build)
            //StasisCoolerBlockEntity.this.setItem(slot, bottle)

            inserted
          else
            0L
    }

    override def extract(resource: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long = {
      StoragePreconditions.notBlankNotNegative(resource, maxAmount)

      this.bottle match
        case None => 0L
        case Some(bottle) =>
          val builder = BottomlessBottleItem.BottomlessBottleContents.Builder.of(BottomlessStorageBlockEntity.this.level, bottle)

          if validVariant(builder.template, resource) then
            this.updateSnapshots(transaction)
            val extracted = builder.extract(resource, maxAmount)
            BottomlessBottleItem.BottomlessBottleContents.replaceInStack(bottle, builder.build)
            //StasisCoolerBlockEntity.this.setItem(slot, bottle)

            extracted
          else
            0L
    }

    override def getResource: FluidVariant =
      this.bottle match
        case None => FluidVariant.blank()
        case Some(b) => BottomlessBottleItem.BottomlessBottleContents.getFromStack(b).variant

    override def isResourceBlank: Boolean = getResource.isBlank

    override def getAmount: Long =
      this.bottle match
        case None => 0L
        case Some(bottle) =>
          BottomlessBottleItem.BottomlessBottleContents.getFromStack(bottle).amount

    override def getCapacity: Long =
      this.bottle match
        case None => 0L
        case Some(bottle) =>
          BottomlessBottleItem.getMaxStackExpensive(bottle)

    override def createSnapshot(): ResourceAmount[FluidVariant] =
      this.bottle match
        case None => ResourceAmount(FluidVariant.blank(), 0L)
        case Some(bottle) =>
          val contents = BottomlessBottleItem.BottomlessBottleContents.getFromStack(bottle)
          ResourceAmount(contents.variant, contents.amount)

    override def readSnapshot(t: ResourceAmount[FluidVariant]): Unit =
      this.bottle match
        case None => ()
        case Some(bottle) =>
          val contents = BottomlessBottleItem.BottomlessBottleContents(t.resource(), t.amount())
          BottomlessBottleItem.BottomlessBottleContents.replaceInStack(bottle, contents)

    override def onFinalCommit(): Unit =
      // better safe than soggy...
      BottomlessStorageBlockEntity.this.setChanged()

  override def loadAdditional(tag: CompoundTag, lookup: HolderLookup.Provider): Unit =
    super.loadAdditional(tag, lookup)
    ContainerHelper.loadAllItems(tag, items, lookup)
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
    items.clear()

  def isEmpty: Boolean =
    items.stream().allMatch(_.isEmpty)

  protected def updateSlot(slot: Int): Unit =
    val stack = this.items.get(slot)
    if stack.is(SpectrumStorageItems.bottomlessBottle) then
      itemStorage.parts.get(slot).resetVariant()
      fluidStorage.parts.get(slot).filter = BottomlessBottleItem.BottomlessBottleContents.getFromStack(stack).variant
    else if stack.is(SpectrumBlocks.BOTTOMLESS_BUNDLE.asItem()) then
      fluidStorage.parts.get(slot).resetVariant()
      itemStorage.parts.get(slot).filter = BundleHelper.contentsFromStack(stack).variant
    else
      fluidStorage.parts.get(slot).resetVariant()
      itemStorage.parts.get(slot).resetVariant()

  protected def updateSlotShown(slot: Int): Unit = ()

  def loadItemFilters(tag: CompoundTag, provider: HolderLookup.Provider): Unit =
    val listTag = tag.getList(BottomlessStorageBlockEntity.tagItemFilters, 10)
    for i <- 0 until listTag.size() do
      val compound = listTag.getCompound(i)
      val j = compound.getByte("Slot").toInt
      if j >= 0 && j < capacity then
        // if error then will auto return blankie wankie
        val variant = ItemVariant.CODEC.decode(provider.createSerializationContext(NbtOps.INSTANCE), compound).getOrThrow()
        itemStorage.parts.get(j).filter = variant.getFirst

  def loadFluidFilters(tag: CompoundTag, provider: HolderLookup.Provider): Unit =
    val listTag = tag.getList(BottomlessStorageBlockEntity.tagFluidFilters, 10)
    for i <- 0 until listTag.size() do
      val compound = listTag.getCompound(i)
      val j = compound.getByte("Slot").toInt
      if j >= 0 && j < capacity then

        val variant = FluidVariant.CODEC.decode(provider.createSerializationContext(NbtOps.INSTANCE), compound).getOrThrow().getFirst
        fluidStorage.parts.get(j).filter = variant

  def saveItemFilters(tag: CompoundTag, provider: HolderLookup.Provider): Unit =
    val listTag = ListTag()
    for i <- 0 until capacity do
      val filter = itemStorage.parts.get(i).filter
      if !filter.isBlank then
        val compound = CompoundTag()
        compound.putByte("Slot", i.toByte)
        val compound2 = ItemVariant.CODEC.encode(filter, provider.createSerializationContext(NbtOps.INSTANCE), compound).getOrThrow()
        listTag.add(compound2)
    tag.put(BottomlessStorageBlockEntity.tagItemFilters, listTag)

  def saveFluidFilters(tag: CompoundTag, provider: HolderLookup.Provider): Unit =
    val listTag = ListTag()
    for i <- 0 until 6 do
      val filter = fluidStorage.parts.get(i).filter
      if !filter.isBlank then
        val compound = CompoundTag()
        compound.putByte("Slot", i.toByte)
        val compound2 = FluidVariant.CODEC.encode(filter, provider.createSerializationContext(NbtOps.INSTANCE), compound).getOrThrow()
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
    if stack.is(SpectrumStorageItems.bottomlessBottle) || stack.is(SpectrumBlocks.BOTTOMLESS_BUNDLE.asItem()) then
      this.items.set(slot, stack)
      this.updateSlot(slot)
      this.updateSlotShown(slot)
    else if stack.isEmpty then
      this.removeItem(slot, 1)

  def stillValid(player: Player): Boolean = Container.stillValidBlockEntity(this, player)

  def getMaxStackSize(stack: ItemStack): Int = 1





abstract class ContainerBottomlessStorageBlockEntity(capacity: Int, baseEntity: BlockEntityType[BottomlessStorageBlockEntity], pos: BlockPos, state: BlockState) extends BottomlessStorageBlockEntity(capacity, baseEntity, pos, state), MenuProvider, NameableBlockEntity:
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

    override def clearContent(): Unit = items.clear()

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
    this.level.setBlock(this.getBlockPos, state.setValue(BarrelBlock.OPEN, open), 3)

  override def getDisplayName: Component = getName


  def getContainerSize: Int = capacity

object BottomlessStorageBlockEntity:
  val tagFluidFilters = "fluid_filters"
  val tagItemFilters = "item_filters"
  final class BottomlessBarrelBlockEntity(pos: BlockPos, state: BlockState) extends ContainerBottomlessStorageBlockEntity(BottomlessStorageMenu.barrelContainerSize, SpectrumStorageBlocks.bottomlessBarrelBlockEntity, pos, state):
    override def defaultName: Component = Component.translatable("container.spectrumstorage.bottomless_barrel")

    override def createMenu(windowId: Int, inventory: Inventory, player: Player): AbstractContainerMenu =
      BottomlessStorageMenu.barrelServer(windowId, inventory, containerView)


  final class BottomlessAmphoraBlockEntity(pos: BlockPos, state: BlockState) extends ContainerBottomlessStorageBlockEntity(BottomlessStorageMenu.amphoraContainerSize, SpectrumStorageBlocks.bottomlessAmphoraBlockEntity, pos, state):
    override def defaultName: Component = Component.translatable("container.spectrumstorage.bottomless_amphora")

    override def createMenu(windowId: Int, inventory: Inventory, player: Player): AbstractContainerMenu =
      BottomlessStorageMenu.amphoraServer(windowId, inventory, containerView)

  object BundleHelper:
    def contentsFromStack(stack: ItemStack): BottomlessStack =
      stack.getOrDefault(SpectrumDataComponentTypes.BOTTOMLESS_STACK, BottomlessStack.DEFAULT)

    def buildFromStack(stack: ItemStack): Builder =
      val prev = stack.getOrDefault(SpectrumDataComponentTypes.BOTTOMLESS_STACK, BottomlessStack.DEFAULT)
      val max = BottomlessBundleItem.getMaxStoredAmount(LumoEnchantmentHelper.getLevelExpensive(Enchantments.POWER, stack))
      val voiding = EnchantmentHelper.hasTag(stack, SpectrumEnchantmentTags.DELETES_OVERFLOW_IN_INVENTORY)
      val locked = stack.has(DataComponents.LOCK)
      new Builder(prev, max, voiding, locked)

    final class Builder(prev: BottomlessStack, val max: Long, val voiding: Boolean, val locked: Boolean):
      var amount: Long = prev.count()
      var template: ItemVariant = prev.variant()

      def isEmpty: Boolean =
        template.isBlank || amount == 0L
      
      def getMaxAllowed(variant: ItemVariant, amount: Long): Long =
        if variant.isBlank || amount <= 0 || (!this.isEmpty && template != variant) then
          0
        else if voiding then
          Int.MaxValue
        else
          this.max - this.amount

      def insert(variant: ItemVariant, amount: Long): Long =
        val added =

          math.min(amount, getMaxAllowed(variant, amount))
        if added == 0 then
          return 0
        if this.isEmpty then
          this.template = variant

        this.amount += added
        added

      def extract(variant: ItemVariant, amount: Long): Long =
        if variant != template then
          0
        else
          val toRemove = math.min(this.amount, amount)
          this.amount -= toRemove
          if this.amount == 0 then
            this.template = ItemVariant.blank()

          toRemove
          
      def set(variant: ItemVariant, amount: Long): Unit =
        if variant.isBlank || amount == 0 then
          this.template = ItemVariant.blank()
          this.amount = 0
        else
          this.template = variant
          this.amount = amount
          
      def buildAndSet(stack: ItemStack): Unit =
        if this.isEmpty then
          stack.remove(SpectrumDataComponentTypes.BOTTOMLESS_STACK)
        else
          stack.set(SpectrumDataComponentTypes.BOTTOMLESS_STACK, new BottomlessStack(template, amount, locked))

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

  def registerStorages(): Unit =
    import SpectrumStorageBlocks.{
      bottomlessAmphoraBlockEntity => bottomlessAmphoraBE,
      bottomlessShelfBlockEntity => bottomlessShelfBE,
      bottomlessBarrelBlockEntity => bottomlessBarrelBE
    }
    List(bottomlessShelfBE, bottomlessAmphoraBE, bottomlessBarrelBE).foreach: it =>
      FluidStorage.SIDED.registerForBlockEntity[BottomlessStorageBlockEntity]((cooler, dir) => {
        cooler.fluidStorage
      }, it)
      ItemStorage.SIDED.registerForBlockEntity[BottomlessStorageBlockEntity]((cooler, dir) => {
        cooler.itemStorage
      }, it)