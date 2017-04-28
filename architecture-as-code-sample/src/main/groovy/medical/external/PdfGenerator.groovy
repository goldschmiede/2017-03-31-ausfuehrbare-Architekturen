package medical.external

/**
 *
 */
class PdfGenerator {

    interface Template {
        def apply(snippets)
    }
    def templates = [:]

    def generateWord(templateName, snippets) {
        def template = templates[templateName] as Template
        template.apply(snippets)
    }
}
