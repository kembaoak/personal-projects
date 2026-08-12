"""
Step 2: Train an LBPH face recognizer on the images captured in dataset/.

Usage:
    python 2_train_model.py

Produces trainer/trainer.yml, which 3_recognize_faces.py loads.
"""

import os

import cv2
import numpy as np

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATASET_DIR = os.path.join(BASE_DIR, "dataset")
TRAINER_DIR = os.path.join(BASE_DIR, "trainer")
MODEL_PATH = os.path.join(TRAINER_DIR, "trainer.yml")


def load_samples():
    detector = cv2.CascadeClassifier(
        cv2.data.haarcascades + "haarcascade_frontalface_default.xml"
    )

    face_samples = []
    ids = []

    for person_dir in sorted(os.listdir(DATASET_DIR)):
        full_dir = os.path.join(DATASET_DIR, person_dir)
        if not os.path.isdir(full_dir):
            continue

        try:
            face_id = int(person_dir.split("_")[0])
        except ValueError:
            print(f"[WARN] Skipping folder with unexpected name: {person_dir}")
            continue

        for filename in os.listdir(full_dir):
            if not filename.lower().endswith((".jpg", ".jpeg", ".png")):
                continue

            path = os.path.join(full_dir, filename)
            img = cv2.imread(path, cv2.IMREAD_GRAYSCALE)
            if img is None:
                continue

            # Images saved by 1_capture_faces.py are already cropped faces,
            # but re-detect in case raw photos were dropped in manually.
            faces = detector.detectMultiScale(img)
            if len(faces) == 0:
                face_samples.append(img)
                ids.append(face_id)
            else:
                for (x, y, w, h) in faces:
                    face_samples.append(img[y : y + h, x : x + w])
                    ids.append(face_id)

    return face_samples, ids


def main():
    if not os.path.isdir(DATASET_DIR) or not os.listdir(DATASET_DIR):
        raise SystemExit(
            "No data found in dataset/. Run 1_capture_faces.py first."
        )

    os.makedirs(TRAINER_DIR, exist_ok=True)

    print("[INFO] Loading face samples...")
    faces, ids = load_samples()
    if not faces:
        raise SystemExit("No faces found in dataset/. Capture some images first.")

    print(f"[INFO] Training on {len(faces)} images across {len(set(ids))} people...")
    recognizer = cv2.face.LBPHFaceRecognizer_create()
    recognizer.train(faces, np.array(ids))
    recognizer.write(MODEL_PATH)

    print(f"[INFO] Training complete. Model saved to {MODEL_PATH}")


if __name__ == "__main__":
    main()
