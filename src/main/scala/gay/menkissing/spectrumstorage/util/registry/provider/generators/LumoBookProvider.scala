package gay.menkissing.spectrumstorage.util.registry.provider.generators

import gay.menkissing.spectrumstorage.util.registry.provider.generators.book.LumoAppendCategory
import net.fabricmc.fabric.api.datagen.v1.{FabricDataGenerator, FabricDataOutput}
import net.minecraft.data.{CachedOutput, DataProvider}
import net.minecraft.resources.ResourceLocation

import java.util.concurrent.CompletableFuture
import scala.collection.mutable

// guidebook pages
abstract class LumoBookProvider(val output: FabricDataOutput) extends DataProvider:
  val appendedCategories = mutable.ListBuffer[LumoAppendCategory]()

  def addToCategory(book: ResourceLocation, category: ResourceLocation): LumoAppendCategory =
    val r = LumoAppendCategory(book, category)
    appendedCategories.append(r)
    r

  def addEntries(): Unit

  override def run(cachedOutput: CachedOutput): CompletableFuture[?] =
    addEntries()
    val appendeds = appendedCategories.map(_.generateAll(cachedOutput, output))
    CompletableFuture.allOf(appendeds.toSeq*)

  override def getName: String = "Spectrum storage book provider"


