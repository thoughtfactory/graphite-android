package com.syncodec.momento.noteComponent.miscellaneous

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

data class TiptapData(
	val type: String,
	val content: List<TiptapContent>
)

data class TiptapContent(
	val type: String?,      //paragraph
	val attrs: TiptapAttrs?,
	val content: List<TiptapInnerContent>?
)

data class TiptapInnerContent(
	val type: String?,  //text, hardbreak
	val marks: List<TiptapMarks>?,
	val text: String?
)

data class TiptapAttrs(
	val textAlign: String
)

data class TiptapMarks(
	val type: String
)

@Preview
@Composable
fun ViewerComponent(
	@PreviewParameter(MockNoteDataList::class)
	noteData: String
) {
	val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	val tiptapData: TiptapData = objectMapper.readValue(noteData)

	SelectionContainer {
		LazyColumn {
			tiptapData.content.forEach { tiptapContent ->
				if (tiptapContent.content!=null) {
					tiptapContent.content.forEach {tiptapInnerContent ->
						when(tiptapInnerContent.type) {
							"text" -> {
								if (tiptapInnerContent.text!=null) {
									item {
										tiptapInnerContent.marks
										Text(
											text = tiptapInnerContent.text
										)
									}
								}
							}
							"hardbreak" -> {
								item {
									Spacer(modifier = Modifier.height(4.dp))
								}
							}
						}
					}
				}
			}
		}

	}

}

class MockNoteDataList : PreviewParameterProvider<List<String>> {
	override val values = sequenceOf(
		listOf(
//			"{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"NGREDIENT\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"▢½ cup curd \\/ yogurt (thick)\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"▢½ tsp turmeric \\/ haldi\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"▢1 tsp kashmiri red chilli powder \\/ lal mirch powder\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"▢½ tsp coriander powder \\/ daniya powder\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"▢¼ tsp cumin powder \\/ jeera powder\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"▢½ tsp garam masala\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"▢½ tsp kasuri methi \\/ dry fenugreek leaves (crushed)\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"▢½ tsp chaat masala\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"▢1 tsp ginger - garlic paste\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"▢¼ tsp ajwain \\/ carom seeds\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"▢2 tsp besan \\/ gram flour (dry roasted)\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"▢1 tbsp lemon juice\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"▢salt to taste\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"▢3 tsp oil\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"vegetables:\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"▢½ onions (petals)\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"▢½ capsicum (red & green, cubed)\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"▢5 cubes paneer \\/ cottage cheese\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"INSTRUCTIONS\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\" \"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"firstly, take ½ cup thick curd \\/ yogurt.\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"further add in all the spices along with salt.\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"mix till all the spices are combined well with curd.\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"now add ½ onion petals, ½ cubed capsicum (red & green) and 5 cubes paneer.\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"also add 1 tsp of oil.\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"mix gently till all the vegetables are coated well.\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"furthermore, to marinate, cover and refrigerate for 30 minutes.\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"after marination, insert the marinated paneer, capsicum and onions into wooden skewers.\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"further, roast it on a hot tawa or grill in oven or tandoor.\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"finally, sprinkle some chaat masala and serve paneer tikka immediately.\"}]}]}"		)
			"{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"“You've gotta dance like there's nobody watching,\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"Love like you'll never be hurt,\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"Sing like there's nobody listening,\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"And live like it's heaven on earth.”\"}]},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"― William W. Purkey\"}]}]}"
		)
	)
}
