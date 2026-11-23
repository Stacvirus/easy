"""Build an editable Word document from ordered OCR lines."""

from io import BytesIO

from docx import Document


def render_docx(pages: list[list[str]]) -> bytes:
    """Preserve line paragraphs and insert a page break between source pages."""
    document = Document()
    for index, lines in enumerate(pages):
        if index:
            document.add_page_break()
        for line in lines:
            document.add_paragraph(line)
    output = BytesIO()
    document.save(output)
    return output.getvalue()
