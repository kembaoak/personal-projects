import math
from pathlib import Path

import pytest

from tfidf import (
    cosine_similarity,
    document_frequency,
    inverse_document_frequency,
    term_frequency,
    tfidf_vectors,
    tokenize,
)

def test_tokenize_lowercases_and_strips_punctuation():
    assert tokenize("The Cat, the CAT!") == ["the", "cat", "the", "cat"]


def test_term_frequency_counts_raw_occurrences():
    tf = term_frequency(["a", "b", "a", "c", "a"])
    assert tf == {"a": 3, "b": 1, "c": 1}


def test_document_frequency_counts_documents_containing_term():
    docs = [["a", "b"], ["a"], ["b", "c"]]
    df = document_frequency(docs)
    assert df == {"a": 2, "b": 2, "c": 1}


def test_inverse_document_frequency_matches_smoothed_formula():
    df = {"a": 2, "c": 1}
    idf = inverse_document_frequency(df, n_docs=3)
    assert idf["a"] == pytest.approx(math.log(4 / 3) + 1)
    assert idf["c"] == pytest.approx(math.log(4 / 2) + 1)


def test_cosine_similarity_identical_vectors_is_one():
    vec = {"a": 0.6, "b": 0.8}
    assert cosine_similarity(vec, vec) == pytest.approx(1.0)


def test_cosine_similarity_orthogonal_vectors_is_zero():
    assert cosine_similarity({"a": 1.0}, {"b": 1.0}) == pytest.approx(0.0)


def test_tfidf_vectors_are_l2_normalized():
    docs = ["the cat sat", "the dog ran", "cats and dogs"]
    vectors = tfidf_vectors(docs)
    for vec in vectors:
        norm = math.sqrt(sum(v * v for v in vec.values()))
        assert norm == pytest.approx(1.0)


def test_tfidf_matches_sklearn_defaults():
    sklearn = pytest.importorskip("sklearn")
    from sklearn.feature_extraction.text import TfidfVectorizer

    docs = [
        "the cat sat on the mat",
        "the dog ran in the yard",
        "cats and dogs are common pets",
    ]

    ours = tfidf_vectors(docs)

    vectorizer = TfidfVectorizer(token_pattern=r"[a-z0-9]+")
    sklearn_matrix = vectorizer.fit_transform(docs).toarray()
    vocab = vectorizer.get_feature_names_out()

    for i, vec in enumerate(ours):
        for j, term in enumerate(vocab):
            expected = sklearn_matrix[i][j]
            actual = vec.get(term, 0.0)
            assert actual == pytest.approx(expected, abs=1e-9)
