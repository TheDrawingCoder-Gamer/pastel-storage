package gay.menkissing.spectrumstorage.util.registry.provider.generators.book

import com.klikli_dev.modonomicon.api.datagen.book.{BookCategoryModel, BookEntryModel}
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.core.HolderLookup
import net.minecraft.data.{CachedOutput, DataProvider}
import net.minecraft.data.PackOutput.Target
import net.minecraft.resources.ResourceLocation

import java.util.concurrent.CompletableFuture
import scala.collection.mutable

class LumoAppendCategory(val book: ResourceLocation, val category: ResourceLocation):
  val entries = mutable.ListBuffer[BookEntryModel]()

  def newEntry(id: ResourceLocation, name: String): BookEntryModel =
    val r = BookEntryModel.create(id, name).withCategory(BookCategoryModel.create(category, ""))
    entries.append(r)
    r

  def withNewEntry(id: ResourceLocation, name: String)(f: BookEntryModel => Unit): this.type =
    val r = newEntry(id, name)
    f(r)
    this

  def generateAll(cache: CachedOutput, output: FabricDataOutput, lookup: HolderLookup.Provider): CompletableFuture[?] =
    val path =
      output.getOutputFolder(Target.DATA_PACK)
            .resolve(book.getNamespace)
            .resolve("modonomicon/books")
            .resolve(book.getPath)
            .resolve("entries")

    CompletableFuture.allOf(
      this.entries.map: entry =>
        DataProvider.saveStable(cache, entry.toJson(lookup), path.resolve(entry.getId.getPath + ".json"))
      .toSeq*
    )

