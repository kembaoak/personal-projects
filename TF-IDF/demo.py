"""Run the from-scratch TF-IDF implementation against sample_data/ and print results."""

from pathlib import Path

from tfidf import cosine_similarity, tfidf_vectors, top_terms, tokenize, term_frequency

SAMPLE_DIR = Path(__file__).parent / "sample_data"


def load_docs():
    paths = sorted(SAMPLE_DIR.glob("*.txt"))
    return [p.name for p in paths], [p.read_text() for p in paths]


def main():
    names, docs = load_docs()
    vectors = tfidf_vectors(docs)

    print("=== Top TF-IDF keywords per document ===")
    for name, vec in zip(names, vectors):
        keywords = ", ".join(f"{term} ({score:.3f})" for term, score in top_terms(vec))
        print(f"{name}: {keywords}")

    print("\n=== Pairwise cosine similarity ===")
    for i in range(len(names)):
        for j in range(i + 1, len(names)):
            sim = cosine_similarity(vectors[i], vectors[j])
            print(f"{names[i]} <-> {names[j]}: {sim:.3f}")

    print("\n=== Search demo ===")
    query = "energetic dog playing fetch"
    query_vec = {
        term: freq for term, freq in term_frequency(tokenize(query)).items()
    }
    ranked = sorted(
        zip(names, vectors),
        key=lambda pair: cosine_similarity(query_vec, pair[1]),
        reverse=True,
    )
    print(f"Query: {query!r}")
    for name, vec in ranked:
        print(f"  {name}: {cosine_similarity(query_vec, vec):.3f}")


if __name__ == "__main__":
    main()
