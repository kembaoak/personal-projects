"""
Real-time banana detection using YOLOv8 (pretrained on COCO, which includes
a "banana" class) and your laptop camera. Unlike a color-based tracker, this
actually recognizes the object rather than just matching color/shape, so it
only draws a box for real bananas.

Setup (from the computer_vision project root, with your venv active):
    pip install -r track_a_banana/requirements.txt

The first run downloads the yolov8n.pt weights (~6MB) automatically, which
requires an internet connection.

Usage:
    python track_banana.py
    python track_banana.py --camera 1 --confidence 0.5

Press 'q' to quit.
"""

import argparse

import cv2
from ultralytics import YOLO

BANANA_CLASS_NAME = "banana"


def main():
    parser = argparse.ArgumentParser(description="Real-time banana detection.")
    parser.add_argument("--camera", type=int, default=0, help="Camera index.")
    parser.add_argument(
        "--confidence",
        type=float,
        default=0.4,
        help="Minimum detection confidence (0-1) to draw a box.",
    )
    args = parser.parse_args()

    print("[INFO] Loading YOLOv8 model (downloads weights on first run)...")
    model = YOLO("yolov8n.pt")

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

    print("[INFO] Looking for bananas. Press 'q' to quit.")

    while True:
        ok, frame = cam.read()
        if not ok:
            print("[WARN] Failed to read frame from camera.")
            break

        results = model(frame, verbose=False)[0]

        for box in results.boxes:
            class_name = model.names[int(box.cls[0])]
            confidence = float(box.conf[0])
            if class_name != BANANA_CLASS_NAME or confidence < args.confidence:
                continue

            x1, y1, x2, y2 = (int(v) for v in box.xyxy[0])
            cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 255), 2)
            cv2.putText(
                frame,
                f"Banana ({confidence:.0%})",
                (x1, y1 - 10),
                cv2.FONT_HERSHEY_SIMPLEX,
                0.7,
                (0, 255, 255),
                2,
            )

        cv2.imshow("Banana Detection - press q to quit", frame)
        if cv2.waitKey(1) & 0xFF == ord("q"):
            break

    cam.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()
