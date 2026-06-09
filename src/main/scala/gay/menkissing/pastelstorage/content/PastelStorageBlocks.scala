package gay.menkissing.pastelstorage.content

import earth.terrarium.pastel.registries.PastelItemGroups
import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.content.block.BottomlessStorageBlock.{BottomlessAmphoraBlock, BottomlessBarrelBlock}
import gay.menkissing.pastelstorage.content.block.{BottomlessShelfBlock, FilterChestBlock}
import gay.menkissing.pastelstorage.content.block.entity.{BottomlessShelfBlockEntity, BottomlessStorageBlockEntity, FilterChestBlockEntity}
import gay.menkissing.pastelstorage.util.resources.{*, given}
import net.minecraft.core.Registry
import net.minecraft.core.registries.{BuiltInRegistries, Registries}
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.world.item.{Item, ItemStack}
import net.minecraft.world.level.block.{Block, SoundType}
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.level.block.state.BlockBehaviour
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.registries.{DeferredBlock, DeferredHolder, DeferredItem, DeferredRegister}

import scala.collection.mutable
import scala.jdk.CollectionConverters.*

object PastelStorageBlocks:
  private val blockItems = mutable.Map[ResourceLocation, mutable.ArrayBuffer[DeferredItem[?]]]()


  val blockItemRegistrar = DeferredRegister.createItems(PastelStorage.ModId)
  val blockRegistrar = DeferredRegister.createBlocks(PastelStorage.ModId)
  val blockEntityRegister = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PastelStorage.ModId)

  //def makeItem(rl: ResourceLocation, item: Item): Item =
  //  blockItems += item
  //  Registry.register(BuiltInRegistries.ITEM, rl, item)

  def makeEntity[T <: BlockEntity](name: String, factory: BlockEntityType.BlockEntitySupplier[T], blocks: => Block*): DeferredHolder[BlockEntityType[?], BlockEntityType[T]] =
    // Seems like null is ok here, if it's null then it won't check data fixers?
    blockEntityRegister.register(name, () => BlockEntityType.Builder.of[T](factory, blocks *).build(null))

  def register[T <: Block](name: String, tab: ResourceLocation, block: => T): DeferredBlock[T] =
    val it = blockRegistrar.register(name, () => block)
    val item = blockItemRegistrar.registerSimpleBlockItem(it, Item.Properties())
    blockItems.getOrElseUpdate(tab, mutable.ArrayBuffer.empty).addOne(item)
    it

  val bottomlessShelf: DeferredBlock[Block] =
    register("bottomless_shelf", PastelItemGroups.INSTRUMENTS_ID, BottomlessShelfBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(1.5f)))
    /*
    InfoCollector.instance.block(SpectrumStorage.locate("bottomless_shelf"),
      BottomlessShelfBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(1.5f)))
                 .lang("Bottomless Shelf")
                 .item()
                 .model(gen => item => gen.withExistingParent(item, bottomlessShelf.modelLoc.withSuffix("_inventory"))).build()
                 .tag(BlockTags.MINEABLE_WITH_AXE)
                 .dropSelf()
                 .blockstate(ComplexBlockstateDatagen.BottomlessShelf.genBottomlessShelf)
                 .registerItemInGroup(ItemGroupIDs.SUBTAB_FUNCTIONAL)

     */

  val bottomlessBarrel =
    register("bottomless_barrel", PastelItemGroups.INSTRUMENTS_ID, BottomlessBarrelBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(1.5f)))

    /*
    InfoCollector.instance.block(SpectrumStorage.locate("bottomless_barrel"),
      BottomlessBarrelBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(1.5f)))
                 .lang("Bottomless Barrel")
                 .tag(BlockTags.MINEABLE_WITH_AXE)
                 .simpleItem()
                 .dropSelf()
                 .blockstate(gen => block => gen.barrelBlock(block))
                 .registerItemInGroup(ItemGroupIDs.SUBTAB_FUNCTIONAL)
    */
  val bottomlessAmphora =
    register("bottomless_amphora", PastelItemGroups.INSTRUMENTS_ID, BottomlessAmphoraBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(4.0f)))
  /*
    InfoCollector.instance.block(SpectrumStorage.locate("bottomless_amphora"),
      BottomlessAmphoraBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(4.0f)))
                 .lang("Bottomless Amphora")
                 .tag(BlockTags.MINEABLE_WITH_AXE)
                 .simpleItem()
                 .dropSelf()
                 .blockstate(gen => block => gen.barrelBlock(block))
                 .registerItemInGroup(ItemGroupIDs.SUBTAB_FUNCTIONAL)
    */
  val filterChest =
    register("filter_chest", PastelItemGroups.INSTRUMENTS_ID, FilterChestBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(4.0f).noOcclusion()))
    /*
    InfoCollector.instance.block(SpectrumStorage.locate("filter_chest"),
      FilterChestBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(4.0f).noOcclusion()))
                 .lang("Filter Barrel")
                 .tag(BlockTags.MINEABLE_WITH_AXE)
                 .simpleItem()
                 .dropSelf()
                 .blockstate(gen => block => gen.barrelBlockExistingModel(block))
                 .registerItemInGroup(ItemGroupIDs.SUBTAB_FUNCTIONAL)

   */

  val bottomlessShelfBlockEntity =
    makeEntity("bottomless_shelf", (a, b) => BottomlessShelfBlockEntity(a, b), bottomlessShelf.get())
  val bottomlessBarrelBlockEntity =
    makeEntity("bottomless_barrel", (a, b) => BottomlessStorageBlockEntity.BottomlessBarrelBlockEntity(a, b), bottomlessBarrel.get())
  val bottomlessAmphoraBlockEntity =
    makeEntity("bottomless_amphora", (a, b) => BottomlessStorageBlockEntity.BottomlessAmphoraBlockEntity(a, b), bottomlessAmphora.get())

  val filterChestBlockEntity =
    makeEntity("filter_chest", (a, b) => FilterChestBlockEntity(a, b), filterChest.get())

  def addItemsToTabs(it: BuildCreativeModeTabContentsEvent): Unit =
    val group = it.getTab
    blockItems.get(it.getTabKey.location()).foreach { items =>
      items.foreach(it.accept)
    }

  def submit(register: IEventBus): Unit =
    BottomlessStorageBlockEntity.registerStorages(register)
    blockRegistrar.register(register)
    blockItemRegistrar.register(register)
    blockEntityRegister.register(register)
    register.addListener(addItemsToTabs)


