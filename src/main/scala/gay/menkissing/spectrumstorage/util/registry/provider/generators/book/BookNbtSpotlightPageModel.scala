package gay.menkissing.spectrumstorage.util.registry.provider.generators.book

import com.google.gson.JsonObject
import com.klikli_dev.modonomicon.api.datagen.book.BookTextHolderModel
import com.klikli_dev.modonomicon.api.datagen.book.condition.{BookConditionModel, BookNoneConditionModel}
import com.klikli_dev.modonomicon.api.datagen.book.page.BookPageModel
import com.mojang.serialization.JsonOps
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.ResourceLocation

class BookNbtSpotlightPageModel(anchor: String, condition: BookConditionModel) extends BookPageModel(BookNbtSpotlightPageModel.id, anchor, condition):
  var item: ItemVariant = ItemVariant.blank()
  var title = BookTextHolderModel("")
  var text = BookTextHolderModel("")

  override def toJson: JsonObject =
    val json = super.toJson
    json.add("title", this.title.toJson)
    json.add("text", this.text.toJson)
    val jsonObj = new JsonObject()
    jsonObj.addProperty("item", BuiltInRegistries.ITEM.getKey(item.getItem).toString)
    val nbt = item.copyNbt()
    if nbt != null then
      val nbtJson = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, nbt)
      jsonObj.add("nbt", nbtJson)
    
    json.add("item", jsonObj)
      
    json

object BookNbtSpotlightPageModel:
  val id = ResourceLocation("spectrum", "nbt_spotlight")
  
  class Builder:
    var condition: BookConditionModel = BookNoneConditionModel()
    var anchor: String = ""
    var item: ItemVariant = ItemVariant.blank()
    var title = BookTextHolderModel("")
    var text = BookTextHolderModel("")
    
    def withCondition(cond: BookConditionModel): this.type =
      this.condition = cond
      this
    
    def withAnchor(x: String): this.type =
      this.anchor = x
      this
    
    def withItem(i: ItemVariant): this.type =
      this.item = i
      this
    
    def withTitle(x: String): this.type =
      this.title = BookTextHolderModel(x)
      this
    
    def withText(x: String): this.type =
      this.text = BookTextHolderModel(x)
      this
      
    def build(): BookNbtSpotlightPageModel =
      val model = BookNbtSpotlightPageModel(this.anchor, this.condition)
      model.item = this.item
      model.title = this.title
      model.text = this.text
      model
