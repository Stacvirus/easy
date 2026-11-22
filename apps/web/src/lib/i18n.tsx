import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from 'react'

export type Language = 'en' | 'fr'

const french: Record<string, string> = {
  'YOUR NOTES, ORGANIZED': 'VOS NOTES, ORGANISÉES',
  'From paper to': 'Du papier aux',
  'possibility.': 'possibilités.',
  'Turn lecture notes and documents into editable Word files, with every original page kept in order.':
    'Transformez vos notes de cours et documents en fichiers Word modifiables, tout en conservant l’ordre des pages originales.',
  '01 Capture': '01 Capturer',
  '02 Organize': '02 Organiser',
  '03 Convert': '03 Convertir',
  'A quieter place for your academic work.':
    'Un espace serein pour votre travail universitaire.',
  WELCOME: 'BIENVENUE',
  BACK: 'À NOUVEAU',
  IN: 'PARMI NOUS',
  'Sign in to your workspace': 'Connectez-vous à votre espace',
  'Create your workspace': 'Créez votre espace',
  'Your documents are right where you left them.':
    'Retrouvez vos documents là où vous les avez laissés.',
  'Start organizing your notes in a few moments.':
    'Commencez à organiser vos notes en quelques instants.',
  'Email address': 'Adresse e-mail',
  Password: 'Mot de passe',
  'Enter your password': 'Saisissez votre mot de passe',
  'Show password': 'Afficher le mot de passe',
  'Hide password': 'Masquer le mot de passe',
  'Sign in': 'Se connecter',
  'Create account': 'Créer un compte',
  'New here?': 'Vous êtes nouveau ?',
  'Already have an account?': 'Vous avez déjà un compte ?',
  'Create an account': 'Créer un compte',
  DIGITIZE: 'NUMÉRISER',
  'Make it editable.': 'Rendez-le modifiable.',
  'Recognize handwriting and create a Word file in your saved page order.':
    'Reconnaissez l’écriture manuscrite et créez un fichier Word dans l’ordre de vos pages.',
  'Document language': 'Langue du document',
  English: 'Anglais',
  Français: 'Français',
  'Conversion queued. You can keep working while it runs.':
    'Conversion lancée. Vous pouvez continuer à travailler.',
  'Converting…': 'Conversion en cours…',
  'Try conversion again': 'Relancer la conversion',
  'Convert to Word': 'Convertir en Word',
  'Handwriting recognition is best effort. Check the Word file after download.':
    'La reconnaissance manuscrite peut comporter des erreurs. Vérifiez le fichier Word après téléchargement.',
  'Waiting to start': 'En attente',
  'Recognizing pages': 'Reconnaissance des pages',
  'Conversion complete': 'Conversion terminée',
  'Conversion failed': 'Échec de la conversion',
  'Please try again.': 'Veuillez réessayer.',
  'Your Word file is ready below.': 'Votre fichier Word est prêt ci-dessous.',
  'This page updates automatically.':
    'Cette page se met à jour automatiquement.',
  Close: 'Fermer',
  'NEW DOCUMENT': 'NOUVEAU DOCUMENT',
  'Give your pages a home.': 'Rassemblez vos pages.',
  'Start with the details. You can add one or several images next.':
    'Commencez par les informations. Vous pourrez ensuite ajouter une ou plusieurs images.',
  'Document title': 'Titre du document',
  'e.g. Week 3 lecture notes': 'Ex. : notes de cours, semaine 3',
  Date: 'Date',
  Subject: 'Matière',
  General: 'Général',
  Cancel: 'Annuler',
  'Create document': 'Créer le document',
  'Delete this document and all its pages and exports?':
    'Supprimer ce document, toutes ses pages et ses fichiers exportés ?',
  'Document deleted.': 'Document supprimé.',
  'Back to library': 'Retour à la bibliothèque',
  'DOCUMENT /': 'DOCUMENT /',
  'Loading document…': 'Chargement du document…',
  'Delete document': 'Supprimer le document',
  page: 'page',
  pages: 'pages',
  'Use JPEG or PNG images under 10 MB each.':
    'Utilisez des images JPEG ou PNG de moins de 10 Mo chacune.',
  'Upload at most 50 MB at a time.': 'Envoyez au maximum 50 Mo à la fois.',
  'A document can have at most 50 pages.':
    'Un document peut contenir au maximum 50 pages.',
  'Page order saved.': 'Ordre des pages enregistré.',
  ORIGINALS: 'ORIGINAUX',
  'Document pages': 'Pages du document',
  'Your source images stay available after conversion.':
    'Vos images originales restent disponibles après la conversion.',
  'Uploading pages…': 'Envoi des pages…',
  'Drop images here, or browse':
    'Déposez des images ici ou parcourez vos fichiers',
  'JPEG or PNG · Up to 10 MB each · 50 pages maximum':
    'JPEG ou PNG · Jusqu’à 10 Mo chacune · 50 pages maximum',
  'Browse files': 'Parcourir les fichiers',
  'Loading pages…': 'Chargement des pages…',
  'PAGE ORDER': 'ORDRE DES PAGES',
  'Drag or use arrows to arrange': 'Glissez ou utilisez les flèches pour trier',
  'Page removed.': 'Page supprimée.',
  'page added.': 'page ajoutée.',
  'pages added.': 'pages ajoutées.',
  'No pages yet': 'Aucune page pour le moment',
  'Add your first image to start this document.':
    'Ajoutez votre première image pour commencer.',
  'Remove page': 'Supprimer la page',
  FILES: 'FICHIERS',
  'Word exports': 'Fichiers Word',
  'Loading exports…': 'Chargement des fichiers…',
  'Editable document': 'Document modifiable',
  'Download Word file': 'Télécharger le fichier Word',
  'Converted files will appear here.':
    'Les fichiers convertis apparaîtront ici.',
  'Original page preview': 'Aperçu de la page originale',
  'Original page': 'Page originale',
  'Close preview': 'Fermer l’aperçu',
  'Original uploaded page': 'Page originale envoyée',
  'YOUR LIBRARY': 'VOTRE BIBLIOTHÈQUE',
  'Everything in its place.': 'Tout est à sa place.',
  'Keep the original pages together, then turn them into an editable document.':
    'Regroupez les pages originales, puis transformez-les en document modifiable.',
  'New document': 'Nouveau document',
  Documents: 'Documents',
  Subjects: 'Matières',
  'From image to editable Word file': 'De l’image au fichier Word modifiable',
  'All documents': 'Tous les documents',
  'General documents': 'Documents généraux',
  document: 'document',
  documents: 'documents',
  'Search documents': 'Rechercher des documents',
  'Filter by date': 'Filtrer par date',
  Clear: 'Effacer',
  'Loading your documents…': 'Chargement de vos documents…',
  'Try again': 'Réessayer',
  'No documents match this view': 'Aucun document ne correspond à cette vue',
  'A fresh page awaits': 'Une nouvelle page vous attend',
  'Try another search, date, or subject.':
    'Essayez une autre recherche, date ou matière.',
  'Create a document, add your images, and start converting.':
    'Créez un document, ajoutez vos images et lancez la conversion.',
  'Subject renamed.': 'Matière renommée.',
  'Subject created.': 'Matière créée.',
  'Subject deleted.': 'Matière supprimée.',
  Delete: 'Supprimer',
  'Documents assigned to it must be removed first.':
    'Les documents associés doivent d’abord être supprimés.',
  WORKSPACE: 'ESPACE DE TRAVAIL',
  SUBJECTS: 'MATIÈRES',
  'Add subject': 'Ajouter une matière',
  'Loading subjects…': 'Chargement des matières…',
  'Subject name': 'Nom de la matière',
  Save: 'Enregistrer',
  'My workspace': 'Mon espace',
  'Sign out': 'Se déconnecter',
  'Drag to reorder': 'Glisser pour réordonner',
  'Drag page': 'Glisser la page',
  'to reorder': 'pour réordonner',
  'Move page': 'Déplacer la page',
  up: 'vers le haut',
  down: 'vers le bas',
  'Preview page': 'Voir la page',
  Page: 'Page',
  'PNG image': 'Image PNG',
  'JPEG image': 'Image JPEG',
  'Move up': 'Monter',
  'Move down': 'Descendre',
  Preview: 'Aperçu',
  'Your documents, in good order.': 'Vos documents, bien organisés.',
  Dismiss: 'Fermer',
  'Document created. Add your pages to begin.':
    'Document créé. Ajoutez vos pages pour commencer.',
  'Cannot reach the server. Check that the API is running.':
    'Connexion au serveur impossible. Vérifiez que l’API fonctionne.',
  'Something went wrong. Please try again.':
    'Une erreur est survenue. Veuillez réessayer.',
}

const initialLanguage = (): Language => {
  const stored = localStorage.getItem('document-language')
  if (stored === 'en' || stored === 'fr') return stored
  return navigator.language.toLowerCase().startsWith('fr') ? 'fr' : 'en'
}

let currentLanguage: Language = initialLanguage()
export const getLanguage = () => currentLanguage

const LanguageContext = createContext<{
  language: Language
  setLanguage: (language: Language) => void
}>({ language: currentLanguage, setLanguage: () => {} })

export function LanguageProvider({ children }: { children: ReactNode }) {
  const [language, updateLanguage] = useState<Language>(currentLanguage)
  useEffect(() => {
    document.documentElement.lang = language
    currentLanguage = language
    localStorage.setItem('document-language', language)
  }, [language])
  function setLanguage(next: Language) {
    currentLanguage = next
    updateLanguage(next)
  }
  return (
    <LanguageContext.Provider value={{ language, setLanguage }}>
      {children}
    </LanguageContext.Provider>
  )
}

export function useI18n() {
  const { language, setLanguage } = useContext(LanguageContext)
  return {
    language,
    setLanguage,
    t: (english: string) =>
      language === 'fr' ? (french[english] ?? english) : english,
  }
}

export function translate(english: string) {
  return currentLanguage === 'fr' ? (french[english] ?? english) : english
}
