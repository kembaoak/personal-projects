"""From-scratch TF-IDF implementation (standard library only)."""

import math
import re
from collections import Counter

_TOKEN_RE = re.compile(r"[a-z0-9]+")


def tokenize(text):
    """Lowercase and split text into alphanumeric tokens."""
    return _TOKEN_RE.findall(text.lower())


def term_frequency(tokens):
    """Return {term: tf} where tf is the raw count of the term in the document."""
    return dict(Counter(tokens))


def document_frequency(tokenized_docs):
    """Return {term: number of documents containing that term}."""
    df = Counter()
    for tokens in tokenized_docs:
        df.update(set(tokens))
    return df


def inverse_document_frequency(df, n_docs):
    """Smoothed IDF: log((1 + n) / (1 + df)) + 1. Matches sklearn's default."""
    return {term: math.log((1 + n_docs) / (1 + count)) + 1 for term, count in df.items()}


def tfidf_vectors(docs):
    """Compute L2-normalized TF-IDF vectors for a list of raw document strings.

    Returns a list of {term: score} dicts, one per document. Matches
    scikit-learn's TfidfVectorizer defaults (smooth_idf=True, norm='l2'),
    which is what tests/test_tfidf.py validates against.
    """
    tokenized_docs = [tokenize(doc) for doc in docs]
    df = document_frequency(tokenized_docs)
    idf = inverse_document_frequency(df, len(docs))

    vectors = []
    for tokens in tokenized_docs:
        tf = term_frequency(tokens)
        raw = {term: freq * idf[term] for term, freq in tf.items()}
        norm = math.sqrt(sum(v * v for v in raw.values()))
        vectors.append({term: v / norm for term, v in raw.items()} if norm else raw)
    return vectors


def cosine_similarity(vec_a, vec_b):
    """Cosine similarity between two sparse {term: score} vectors."""
    shared_terms = set(vec_a) & set(vec_b)
    dot = sum(vec_a[term] * vec_b[term] for term in shared_terms)
    norm_a = math.sqrt(sum(v * v for v in vec_a.values()))
    norm_b = math.sqrt(sum(v * v for v in vec_b.values()))
    if norm_a == 0 or norm_b == 0:
        return 0.0
    return dot / (norm_a * norm_b)


def top_terms(vec, k=5):
    """Return the k highest-weighted (term, score) pairs from a TF-IDF vector."""
    return sorted(vec.items(), key=lambda item: item[1], reverse=True)[:k]
