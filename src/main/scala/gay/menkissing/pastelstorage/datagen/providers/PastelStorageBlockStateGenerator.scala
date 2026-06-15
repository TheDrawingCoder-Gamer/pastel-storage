package gay.menkissing.pastelstorage.datagen.providers

import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.content.PastelStorageBlocks
import gay.menkissing.pastelstorage.content.block.BottomlessShelfBlock
import net.minecraft.core.Direction
import net.minecraft.data.PackOutput
import net.minecraft.data.models.blockstates.{Condition, MultiPartGenerator, VariantProperties}
import net.minecraft.data.models.model.{ModelLocationUtils, ModelTemplate, TextureMapping, TextureSlot}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.properties.{BlockStateProperties, EnumProperty, Property}
import net.neoforged.neoforge.client.model.generators.{BlockModelBuilder, BlockModelProvider, BlockStateProvider, ConfiguredModel, ModelFile, ModelProvider, MultiPartBlockStateBuilder}
import net.neoforged.neoforge.common.data.ExistingFileHelper
import gay.menkissing.pastelstorage.util.resources.{*, given}
import net.neoforged.neoforge.client.model.generators.ModelFile.ExistingModelFile

import scala.collection.mutable

// Had to be completely rewritten because SOMEONE thought it was a good idea to FORCE me to use the forge specific
// generators when the normal vanilla stuff works JUST FINE!
final class PastelStorageBlockStateGenerator(output: PackOutput, existingFileHelper: ExistingFileHelper) extends BlockStateProvider(output, PastelStorage.ModId, existingFileHelper) {
  private class ShelfGenerator(val block: Block):
    import PastelStorageBlockStateGenerator.*
    val cache = mutable.HashMap.empty[CoolerModelSlotKey, BlockModelBuilder]


    def addShelfModelSlot(generator: MultiPartBlockStateBuilder, suffix: String, condition: FuckyCondition[?], rot: Int,
                          slotProp: EnumProperty[BottomlessShelfBlock.ShelfSlotOccupiedBy],
                          template: String)(propValue: BottomlessShelfBlock.ShelfSlotOccupiedBy): Unit =
      val str = s"_${propValue.getSerializedName}"
      val fullSuffix = s"_${propValue.getSerializedName}_$suffix"
      val key = CoolerModelSlotKey(template, str)
      val model = cache.getOrElseUpdate(key, models().withExistingParent(s"bottomless_shelf$fullSuffix", mcLoc("block/" + template)).texture("texture", TextureMapping.getBlockTexture(block, str)))
      generator
        .part()
        .rotationY(rot)
        .modelFile(model)
        .addModel()
        .condition(condition.prop, condition.value)
        .condition(slotProp, propValue)
        .end()

    def addSlotAndRotationVariants(generator: MultiPartBlockStateBuilder, cond: FuckyCondition[?], rot: Int): Unit =
      List(
        (BottomlessShelfBlock.SHELF_SLOT_0_OCCUPIED_BY, "template_chiseled_bookshelf_slot_top_left", "top_left"),
        (BottomlessShelfBlock.SHELF_SLOT_1_OCCUPIED_BY, "template_chiseled_bookshelf_slot_top_mid", "top_mid"),
        (BottomlessShelfBlock.SHELF_SLOT_2_OCCUPIED_BY, "template_chiseled_bookshelf_slot_top_right", "top_right"),
        (BottomlessShelfBlock.SHELF_SLOT_3_OCCUPIED_BY, "template_chiseled_bookshelf_slot_bottom_left", "bottom_left"),
        (BottomlessShelfBlock.SHELF_SLOT_4_OCCUPIED_BY, "template_chiseled_bookshelf_slot_bottom_mid", "bottom_mid"),
        (BottomlessShelfBlock.SHELF_SLOT_5_OCCUPIED_BY, "template_chiseled_bookshelf_slot_bottom_right", "bottom_right")
      ).foreach: (prop, template, suffix) =>
        val freakyFunc = addShelfModelSlot(generator, suffix, cond, rot, prop, template)
        freakyFunc(BottomlessShelfBlock.ShelfSlotOccupiedBy.Empty)
        freakyFunc(BottomlessShelfBlock.ShelfSlotOccupiedBy.Bottle)
        freakyFunc(BottomlessShelfBlock.ShelfSlotOccupiedBy.Bundle)
        freakyFunc(BottomlessShelfBlock.ShelfSlotOccupiedBy.Battery)

    def generateBottomlessShelfModels(): Unit =
      val baseModel = models().getExistingFile(PastelStorage.locate("block/bottomless_shelf"))

      val generator = getMultipartBuilder(block)
      List(
        Direction.NORTH -> 0,
        Direction.EAST -> 90,
        Direction.SOUTH -> 180,
        Direction.WEST -> 270
      ).foreach: (dir, rot) =>
        val cond = FuckyCondition(BlockStateProperties.HORIZONTAL_FACING, dir)
        generator
          .part()
          .rotationY(rot)
          .uvLock(true)
          .modelFile(baseModel)
          .addModel()
          .condition(BlockStateProperties.HORIZONTAL_FACING, dir)
          .end()
        addSlotAndRotationVariants(generator, cond, rot)



  def barrelBlock(block: Block): Unit =
    val sideTexture = TextureMapping.getBlockTexture(block, "_side")
    val topTexture = TextureMapping.getBlockTexture(block, "_top")
    val bottomTexture = TextureMapping.getBlockTexture(block, "_bottom")
    barrelBlock(block, sideTexture, topTexture, bottomTexture)

  def barrelBlock(block: Block, side: ResourceLocation, top: ResourceLocation, bottom: ResourceLocation): Unit =
    val closeModel = models().cubeBottomTop(block.location.getPath, side, bottom, top)
    val openModel = models().cubeBottomTop(block.location.getPath + "_open", side, bottom, top.withSuffix("_open"))
    barrelBlockWithModel(block, closeModel, openModel)

  def barrelBlockWithModel(block: Block, closeModel: ModelFile, openModel: ModelFile): Unit =

    simpleBlockItem(block, closeModel)
    val variantBuilder = getVariantBuilder(block)
    variantBuilder.forAllStates: state =>
      val model = if state.getValue[java.lang.Boolean](BlockStateProperties.OPEN) then openModel else closeModel
      val dir = state.getValue(BlockStateProperties.FACING)
      val xRot =
        dir match
          case Direction.DOWN => 180
          case Direction.UP => 0
          case _ => 90
      val yRot =
        dir match
          case Direction.DOWN => 0
          case Direction.UP => 0
          case Direction.NORTH => 0
          case Direction.SOUTH => 180
          case Direction.WEST => 270
          case Direction.EAST => 90

      ConfiguredModel.builder()
                     .modelFile(model)
                     .rotationX(xRot)
                     .rotationY(yRot)
                     .build()


  override protected def registerStatesAndModels(): Unit =
    val shelfGenerator = ShelfGenerator(PastelStorageBlocks.bottomlessShelf.get())
    shelfGenerator.generateBottomlessShelfModels()


    barrelBlock(PastelStorageBlocks.bottomlessBarrel.get())
    barrelBlock(PastelStorageBlocks.bottomlessAmphora.get())

    barrelBlockWithModel(
      PastelStorageBlocks.filterChest.get(),
      ExistingModelFile(PastelStorageBlocks.filterChest.get().modelLoc, existingFileHelper),
      ExistingModelFile(PastelStorageBlocks.filterChest.get().modelLoc.withSuffix("_open"), existingFileHelper)
    )


}

object PastelStorageBlockStateGenerator:
  // FUCK YOU OLD FORGE!!!!!!
  private final case class FuckyCondition[T <: Comparable[T]](prop: Property[T], value: T)


  private final case class CoolerModelSlotKey(template: String, str: String)