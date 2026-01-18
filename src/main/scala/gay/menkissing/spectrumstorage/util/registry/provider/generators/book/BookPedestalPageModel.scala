package gay.menkissing.spectrumstorage.util.registry.provider.generators.book

import com.google.gson.JsonObject
import com.klikli_dev.modonomicon.api.datagen.book.BookTextHolderModel
import com.klikli_dev.modonomicon.api.datagen.book.condition.{BookConditionModel, BookNoneConditionModel}
import com.klikli_dev.modonomicon.api.datagen.book.page.BookPageModel
import net.minecraft.resources.ResourceLocation

class BookPedestalPageModel(anchor: String, condition: BookConditionModel) extends BookPageModel(BookPedestalPageModel.id, anchor, condition):
  var title: BookTextHolderModel = BookTextHolderModel("")
  var recipeId: String = ""
  var text: BookTextHolderModel = BookTextHolderModel("")

  override def toJson: JsonObject =
    val json = super.toJson
    json.add("title", this.title.toJson)
    json.add("text", this.text.toJson)
    json.addProperty("recipe_id", this.recipeId)
    json


object BookPedestalPageModel:
  val id = ResourceLocation("spectrum", "pedestal_crafting")

  class Builder:
    var anchor: String = ""
    var condition: BookConditionModel = BookNoneConditionModel()
    var title: BookTextHolderModel = BookTextHolderModel("")
    var recipeId: String = ""
    
    var text: BookTextHolderModel = BookTextHolderModel("")

    def withRecipeId(x: String): this.type =
      this.recipeId = x
      this

    def withTitle(x: String): this.type =
      this.title = BookTextHolderModel(x)
      this

    def withAnchor(x: String): this.type =
      this.anchor = x
      this

    def withCondition(cond: BookConditionModel): this.type =
      this.condition = cond
      this
    
    def withText(x: String): this.type =
      this.text = BookTextHolderModel(x)
      this
      
    def build(): BookPedestalPageModel =
      val model = BookPedestalPageModel(this.anchor, this.condition)
      
      model.recipeId = this.recipeId
      model.text = this.text
      model.title = this.title
      
      model
      
    


