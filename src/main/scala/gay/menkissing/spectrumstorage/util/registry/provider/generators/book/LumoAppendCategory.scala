package gay.menkissing.spectrumstorage.util.registry.provider.generators.book

import com.klikli_dev.modonomicon.api.datagen.book.{BookCategoryModel, BookEntryModel}
import gay.menkissing.spectrumstorage.util.registry.book.FalseCategory
import gay.menkissing.spectrumstorage.util.registry.provider.generators.LumoBookProvider
import gay.menkissing.spectrumstorage.util.registry.provider.generators.book.LumoAppendCategory.EntryCreator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.core.HolderLookup
import net.minecraft.data.{CachedOutput, DataProvider, PackOutput}
import net.minecraft.data.PackOutput.Target
import net.minecraft.resources.ResourceLocation

import java.util.concurrent.CompletableFuture
import scala.collection.mutable

final class LumoAppendCategory(val owner: LumoBookProvider, val book: ResourceLocation, val category: ResourceLocation):
  val entries = mutable.ListBuffer[BookEntryModel]()

  def addEntry(location: ResourceLocation, transId: ResourceLocation)(creator: EntryCreator): this.type =
    var pageIndex = 0
    def savePageText(txt: String): String =
      val x = pageIndex
      pageIndex += 1
      val langKey = s"book.${transId.getNamespace}.${book.getPath}.${transId.getPath}.page${x}.text"
      owner.add(langKey, LumoAppendCategory.escapeBody(txt))
      langKey
    val baseEntry =
      BookEntryModel.create(location, "").withCategory(FalseCategory(category))
    val resEntry = creator(savePageText, baseEntry)
    entries += resEntry
    this
    
      

  def generateAll(cache: CachedOutput, output: PackOutput, lookup: HolderLookup.Provider): CompletableFuture[?] =
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

object LumoAppendCategory:
  def escapeBody(body: String): String =
    body.trim.replace("\r", "").replace("\n", "\\\n")
  
  trait EntryCreator:
    def apply(transGetter: String => String, baseEntry: BookEntryModel): BookEntryModel