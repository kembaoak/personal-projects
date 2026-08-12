"""
Step 3: Real-time face recognition using your laptop camera.

Usage:
    python 3_recognize_faces.py
    python 3_recognize_faces.py --camera 1 --confidence-threshold 70

Requires trainer/trainer.yml (run 2_train_model.py first) and labels.txt
(created automatically by 1_capture_faces.py).

Press 'q' to quit.
"""

import argparse
import os

import cv2

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.join(BASE_DIR, "trainer", "trainer.yml")
LABELS_FILE = os.path.join(BASE_DIR, "labels.txt")


def load_labels() -> dict:
    labels = {0: "Unknown"}
    if os.path.exists(LABELS_FILE):
        with open(LABELS_FILE, "r") as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue
                face_id, name = line.split(",", 1)
                labels[int(face_id)] = name
    return labels


def main():
    parser = argparse.ArgumentParser(description="Real-time face recognition.")
    parser.add_argument("--camera", type=int, default=0, help="Camera index.")
    parser.add_argument(
        "--confidence-threshold",
        type=float,
        default=65.0,
        help=(
            "LBPH distance threshold. Lower distance = more confident match. "
            "Predictions above this value are labeled 'Unknown'."
        ),
    )
    args = parser.parse_args()

    if not os.path.exists(MODEL_PATH):
        raise SystemExit(
            f"Model not found at {MODEL_PATH}. Run 1_capture_faces.py then "
            "2_train_model.py first."
        )

    labels = load_labels()

    recognizer = cv2.face.LBPHFaceRecognizer_create()
    recognizer.read(MODEL_PATH)

    detector = cv2.CascadeClassifier(
        cv2.data.haarcascades + "haarcascade_frontalface_default.xml"
    )

    cam = cv2.VideoCapture(args.camera, cv2.CAP_AVFOUNDATION)
    if not cam.isOpened():
        raise RuntimeError(
            f"Could not open camera index {args.camera}. "
            "Try a different --camera value (0, 1, ...)."
        )

    # macOS's AVFoundation backend returns black frames for the first
    # moment while the sensor warms up; discard a few before starting.
    for _ in range(30):
        cam.read()

    print("[INFO] Starting recognition. Press 'q' to quit.")

    while True:
        ok, frame = cam.read()
        if not ok:
            print("[WARN] Failed to read frame from camera.")
            break

        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        faces = detector.detectMultiScale(gray, scaleFactor=1.2, minNeighbors=5)

        for (x, y, w, h) in faces:
            face_id, distance = recognizer.predict(gray[y : y + h, x : x + w])

            if distance <= args.confidence_threshold:
                name = labels.get(face_id, "Unknown")
                confidence_pct = round(100 - distance)
            else:
                name = "Unknown"
                confidence_pct = round(100 - distance) if distance < 100 else 0

            cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)
            cv2.putText(
                frame,
                f"{name} ({confidence_pct}%)",
                (x + 5, y - 10),
                cv2.FONT_HERSHEY_SIMPLEX,
                0.8,
                (0, 255, 0),
                2,
            )

        cv2.imshow("Face Recognition - press q to quit", frame)
        if cv2.waitKey(1) & 0xFF == ord("q"):
            break

    cam.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()
