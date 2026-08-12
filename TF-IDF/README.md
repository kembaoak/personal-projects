# TF-IDF From Scratch

A pure-Python (standard library only) implementation of TF-IDF, built to
understand the algorithm rather than to use it as a black box.

## What is TF-IDF?

TF-IDF (Term Frequency - Inverse Document Frequency) scores how important a
word is to a document within a collection of documents (a *corpus*). A word
gets a high score when it appears often in one document but rarely across the
rest of the corpus — that combination is what makes it distinctive.

- **TF** (term frequency): how many times a term appears in a document.
- **IDF** (inverse document frequency): a downweighting factor for terms that
  appear in many documents (e.g. "the", "and") and are therefore less useful
  for distinguishing documents.

This implementation uses the same formulas as scikit-learn's
`TfidfVectorizer` defaults, so the from-scratch numbers can be checked
against a trusted library:

```
tf(t, d)  = raw count of term t in document d
idf(t)    = ln((1 + n) / (1 + df(t))) + 1        # smoothed, avoids div-by-zero
tfidf(t, d) = tf(t, d) * idf(t)
```

Each document's final vector is then L2-normalized (divided by its own
Euclidean norm) so document length doesn't skew comparisons.

## Files

- `tfidf.py` — the implementation: `tokenize`, `term_frequency`,
  `document_frequency`, `inverse_document_frequency`, `tfidf_vectors`,
  `cosine_similarity`, `top_terms`.
- `demo.py` — runs the implementation against `sample_data/` and prints top
  keywords per document, pairwise similarity, and a tiny search example.
- `sample_data/` — three short example documents.
- `tests/test_tfidf.py` — unit tests for each building block, plus a
  validation test that cross-checks output against `sklearn.TfidfVectorizer`.

## Running it

No cloud instance needed — this all runs locally on a laptop.

```bash
cd TF-IDF
python3 -m venv .venv && source .venv/bin/activate   # optional but recommended
pip install -r requirements.txt

python demo.py        # see it in action
pytest tests/          # verify correctness, including against sklearn
```
