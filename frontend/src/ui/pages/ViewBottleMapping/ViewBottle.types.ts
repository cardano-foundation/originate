export interface ResponseViewBottole {
  error: ViewBottoleEntry[];
  scheduled: ViewBottoleEntry[];
  success: ViewBottoleEntry[];
}
interface ViewBottoleEntry {
  id: string;
  lotId: string;
  reelNumber: string;
  sequentialNumber: string;
  certificateId: string;
}

export interface ResponseViewBottoleError {
  code: string;
  message: string;
  internalMessage: string;
}
