package gay.menkissing.spectrumstorage.util.registry.provider.generators.book

import com.google.gson.JsonObject
import com.klikli_dev.modonomicon.api.datagen.book.BookTextHolderModel
import com.klikli_dev.modonomicon.api.datagen.book.condition.{BookConditionModel, BookNoneConditionModel}
import com.klikli_dev.modonomicon.api.datagen.book.page.BookPageModel
import net.minecraft.core.HolderLookup
import net.minecraft.resources.ResourceLocation

final class BookPedestalPageModel extends BookPageModel[BookPedestalPageModel](BookPedestalPageModel.id):
  var title: BookTextHolderModel = BookTextHolderModel("")
  var recipeId: String = ""
  var text: BookTextHolderModel = BookTextHolderModel("")

  override def toJson(entry: ResourceLocation, provider: HolderLookup.Provider): JsonObject =
    val json = super.toJson(entry, provider)
    json.add("title", this.title.toJson(provider))
    json.add("text", this.text.toJson(provider))
    json.addProperty("recipe_id", this.recipeId)
    json
  
  def withTitle(title: String): this.type =
    this.title = BookTextHolderModel(title)
    this
  
  def withRecipeId(x: String): this.type =
    this.recipeId = x
    this
  
  def withText(text: String): this.type =
    this.text = BookTextHolderModel(text)
    this


object BookPedestalPageModel:
  val id = ResourceLocation.fromNamespaceAndPath("spectrum", "pedestal_crafting")

  def create(): BookPedestalPageModel = BookPedestalPageModel()
      
    


